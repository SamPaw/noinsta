package `in`.platesight.noinsta.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairingClaimRequest(
    @SerialName("pairing_code") val pairingCode: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("device_type") val deviceType: String = "ANDROID"
)

@Serializable
data class PairingClaimResponse(
    @SerialName("device_id") val deviceId: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user_id") val userId: String,
    @SerialName("message") val message: String
)
