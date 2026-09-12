package `in`.platesight.noinsta.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_events",
    indices = [Index(value = ["client_event_id"], unique = true)]
)
data class PendingEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "client_event_id") val clientEventId: String,
    @ColumnInfo(name = "event_type") val eventType: String,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "occurred_at") val occurredAt: String,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
