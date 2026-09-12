package `in`.platesight.noinsta.data.remote

import `in`.platesight.noinsta.data.remote.model.AppEventRequest
import `in`.platesight.noinsta.data.remote.model.AppEventResponse
import `in`.platesight.noinsta.data.remote.model.AuthResponse
import `in`.platesight.noinsta.data.remote.model.HealthResponse
import `in`.platesight.noinsta.data.remote.model.PairingClaimRequest
import `in`.platesight.noinsta.data.remote.model.PairingClaimResponse
import `in`.platesight.noinsta.data.remote.model.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/pairing/claim")
    suspend fun claimPairing(@Body request: PairingClaimRequest): Response<PairingClaimResponse>

    @POST("api/v1/events")
    suspend fun sendEvent(@Body request: AppEventRequest): Response<AppEventResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/v1/auth/revoke")
    suspend fun revokeToken(): Response<Unit>

    @GET("api/v1/health")
    suspend fun checkHealth(): Response<HealthResponse>
}
