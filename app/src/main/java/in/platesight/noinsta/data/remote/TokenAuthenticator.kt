package `in`.platesight.noinsta.data.remote

import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.model.RefreshTokenRequest
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
    private val apiServiceProvider: Provider<ApiService>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = securePreferencesManager.refreshToken ?: return null

        return runBlocking {
            mutex.withLock {
                // Check if the token was already refreshed by another thread
                val currentToken = securePreferencesManager.accessToken
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (currentToken != requestToken) {
                    // Token was already refreshed, retry with the new token
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                // Call refresh API
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
                            .build()
                    }
                }

                // If refresh fails, clear tokens and let the user re-pair
                securePreferencesManager.clear()
                null
            }
        }
    }
}
