package `in`.platesight.noinsta.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsSummaryDto(
    @SerialName("total_opens_today") val totalOpensToday: Int = 0,
    @SerialName("total_time_today_seconds") val totalTimeTodaySeconds: Int = 0,
    @SerialName("total_interventions_today") val totalInterventionsToday: Int = 0,
    @SerialName("total_opens_all_time") val totalOpensAllTime: Int = 0,
    @SerialName("total_sessions_all_time") val totalSessionsAllTime: Int = 0,
    @SerialName("total_interventions_all_time") val totalInterventionsAllTime: Int = 0,
    @SerialName("last_opened_at") val lastOpenedAt: String? = null
)

@Serializable
data class DailyBreakdownDto(
    @SerialName("date") val date: String,
    @SerialName("opens") val opens: Int = 0,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int = 0,
    @SerialName("interventions") val interventions: Int = 0
)

@Serializable
data class EventLogItemDto(
    @SerialName("id") val id: String,
    @SerialName("event_type") val eventType: String,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("device_name") val deviceName: String = "Unknown Device",
    @SerialName("device_type") val deviceType: String = "ANDROID",
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("client_event_id") val clientEventId: String? = null
)

@Serializable
data class InterventionLogItemDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("sent_at") val sentAt: String,
    @SerialName("acknowledged_at") val acknowledgedAt: String? = null,
    @SerialName("status") val status: String,
    @SerialName("laptop_device_id") val laptopDeviceId: String,
    @SerialName("laptop_device_name") val laptopDeviceName: String = "Unknown Laptop",
    @SerialName("duration_to_ack_seconds") val durationToAckSeconds: Double? = null
)

@Serializable
data class AnalyticsResponse(
    @SerialName("summary") val summary: AnalyticsSummaryDto = AnalyticsSummaryDto(),
    @SerialName("daily_breakdown") val dailyBreakdown: List<DailyBreakdownDto> = emptyList(),
    @SerialName("recent_events") val recentEvents: List<EventLogItemDto> = emptyList(),
    @SerialName("recent_interventions") val recentInterventions: List<InterventionLogItemDto> = emptyList()
)
