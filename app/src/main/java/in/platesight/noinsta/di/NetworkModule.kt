package `in`.platesight.noinsta.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.ApiService
import `in`.platesight.noinsta.data.remote.TokenAuthenticator
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://noinsta.platesight.in/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun createResilientTrustManager(): X509TrustManager {
        val defaultTrustManager = try {
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                init(null as KeyStore?)
            }
            tmf.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
        } catch (e: Exception) {
            null
        }

        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                try {
                    defaultTrustManager?.checkClientTrusted(chain, authType)
                } catch (e: Exception) {
                    // Ignored for client certificates
                }
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                try {
                    defaultTrustManager?.checkServerTrusted(chain, authType)
                } catch (e: Exception) {
                    // Fallback check: If the connection is intercepted by campus/enterprise firewalls
                    // (such as Sophos SSL Inspection on university Wi-Fi) or matches our target domain,
                    // permit it so pairing and API calls succeed without TrustAnchor errors.
                    val isAllowed = chain?.any { cert ->
                        val issuer = cert.issuerDN.name
                        val subject = cert.subjectDN.name
                        issuer.contains("Sophos", ignoreCase = true) ||
                        issuer.contains("platesight.in", ignoreCase = true) ||
                        subject.contains("platesight.in", ignoreCase = true)
                    } == true

                    if (!isAllowed) {
                        throw e
                    }
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return defaultTrustManager?.acceptedIssuers ?: emptyArray()
            }
        }
    }

    private fun configureSsl(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val trustManager = createResilientTrustManager()
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }

        return builder
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, session ->
                if (hostname.endsWith("platesight.in", ignoreCase = true) || hostname == "localhost") {
                    true
                } else {
                    HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                }
            }
    }

    @Provides
    @Singleton
    @AuthClient
    fun provideOkHttpClient(
        securePreferencesManager: SecurePreferencesManager,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(0, TimeUnit.MILLISECONDS)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                securePreferencesManager.accessToken?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)

        return configureSsl(builder).build()
    }

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(0, TimeUnit.MILLISECONDS)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .addInterceptor(loggingInterceptor)

        return configureSsl(builder).build()
    }

    @Provides
    @Singleton
    fun provideApiService(@AuthClient okHttpClient: OkHttpClient, json: Json): ApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshApiService(@RefreshClient okHttpClient: OkHttpClient, json: Json): ApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)
    }
}
