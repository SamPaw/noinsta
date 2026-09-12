package `in`.platesight.noinsta.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppEventRequest(
    @SerialName("event_type") val eventType: String,
    @SerialName("occurred_at") val occurredAt: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("client_event_id") val clientEventId: String? = null
)

@Serializable
data class AppEventResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("intervention_triggered") val interventionTriggered: Boolean? = null,
    @SerialName("eligible_laptops_count") val eligibleLaptopsCount: Int? = null,
    @SerialName("message") val message: String? = null
)
