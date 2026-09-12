package `in`.platesight.noinsta.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.platesight.noinsta.data.local.entity.PendingEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingEventDao {
    @Query("SELECT * FROM pending_events ORDER BY occurred_at ASC")
    fun getAllPendingEventsFlow(): Flow<List<PendingEvent>>

    @Query("SELECT * FROM pending_events ORDER BY occurred_at ASC")
    suspend fun getAllPendingEvents(): List<PendingEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PendingEvent): Long

    @Update
    suspend fun updateEvent(event: PendingEvent)

    @Delete
    suspend fun deleteEvent(event: PendingEvent)

    @Query("DELETE FROM pending_events WHERE client_event_id = :clientEventId")
    suspend fun deleteByClientEventId(clientEventId: String)
}
