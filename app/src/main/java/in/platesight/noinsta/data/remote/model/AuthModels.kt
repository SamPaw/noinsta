package `in`.platesight.noinsta.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null
)

@Serializable
data class HealthResponse(
    @SerialName("status") val status: String,
    @SerialName("version") val version: String? = null
)
