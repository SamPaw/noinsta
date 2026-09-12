package `in`.platesight.noinsta.data.remote

import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.model.RefreshTokenRequest
import `in`.platesight.noinsta.di.RefreshClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val securePreferencesManager: SecurePreferencesManager,
    @RefreshClient private val apiServiceProvider: Provider<ApiService>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Stop if we've already tried to refresh for this request
        if (response.request.header(HEADER_RETRY_COUNT) != null) {
            return null
        }

        val refreshToken = securePreferencesManager.refreshToken ?: return null

        return runBlocking {
            mutex.withLock {
                // Check if the token was already refreshed by another thread
                val currentToken = securePreferencesManager.accessToken
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (currentToken != requestToken && currentToken != null) {
                    // Token was already refreshed, retry with the new token
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .header(HEADER_RETRY_COUNT, "1")
                        .build()
                }

                // Call refresh API using the unauthenticated client
                val apiService = apiServiceProvider.get()
                val refreshResponse = try {
                    apiService.refreshToken(RefreshTokenRequest(refreshToken))
                } catch (e: Exception) {
                    null
                }

                if (refreshResponse != null && refreshResponse.isSuccessful) {
                    val authResponse = refreshResponse.body()
                    if (authResponse != null) {
                        securePreferencesManager.accessToken = authResponse.accessToken
                        authResponse.refreshToken?.let {
                            securePreferencesManager.refreshToken = it
                        }
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${authResponse.accessToken}")
                            .header(HEADER_RETRY_COUNT, "1")
                            .build()
                    }
                } else if (refreshResponse?.code() == 401) {
                    // Refresh token invalid/expired
                    securePreferencesManager.clear()
                    return@runBlocking null
                }

                // For other errors, don't clear tokens, just stop retrying for this request
                null
            }
        }
    }

    companion object {
        private const val HEADER_RETRY_COUNT = "X-Retry-Count"
    }
}
