package `in`.platesight.noinsta.data.remote

import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.model.AuthResponse
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response as RetrofitResponse
import javax.inject.Provider

class TokenAuthenticatorTest {

    private lateinit var securePreferencesManager: SecurePreferencesManager
    private lateinit var apiService: ApiService
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setup() {
        securePreferencesManager = mock()
        apiService = mock()
        val provider = Provider { apiService }
        authenticator = TokenAuthenticator(securePreferencesManager, provider)
    }

    @Test
    fun `successful refresh returns new request`() = runBlocking {
        whenever(securePreferencesManager.refreshToken).thenReturn("old_refresh")
        whenever(securePreferencesManager.accessToken).thenReturn("old_access")
        
        val newAuthResponse = AuthResponse("new_access", "new_refresh")
        whenever(apiService.refreshToken(any())).thenReturn(RetrofitResponse.success(newAuthResponse))

        val originalRequest = Request.Builder()
            .url("https://example.com")
            .header("Authorization", "Bearer old_access")
            .build()
        
        val response = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertEquals("Bearer new_access", authenticatedRequest?.header("Authorization"))
        verify(securePreferencesManager).accessToken = "new_access"
        verify(securePreferencesManager).refreshToken = "new_refresh"
    }

    @Test(timeout = 5000)
    fun `failed refresh clears state and returns null without hanging`() = runBlocking {
        whenever(securePreferencesManager.refreshToken).thenReturn("old_refresh")
        whenever(securePreferencesManager.accessToken).thenReturn("old_access")
        
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        whenever(apiService.refreshToken(any())).thenReturn(RetrofitResponse.error(401, errorBody))

        val originalRequest = Request.Builder()
            .url("https://example.com")
            .header("Authorization", "Bearer old_access")
            .build()
        
        val response = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNull(authenticatedRequest)
        verify(securePreferencesManager).clear()
    }

    @Test
    fun `does not attempt refresh if already retried`() = runBlocking {
        val originalRequest = Request.Builder()
            .url("https://example.com")
            .header("Authorization", "Bearer old_access")
            .header("X-Retry-Count", "1")
            .build()
        
        val response = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNull(authenticatedRequest)
    }
}
