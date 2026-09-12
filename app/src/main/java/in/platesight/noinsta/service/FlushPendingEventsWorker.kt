package `in`.platesight.noinsta.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import `in`.platesight.noinsta.data.local.dao.PendingEventDao
import `in`.platesight.noinsta.data.remote.ApiService
import `in`.platesight.noinsta.data.remote.model.AppEventRequest
import timber.log.Timber

@HiltWorker
class FlushPendingEventsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pendingEventDao: PendingEventDao,
    private val apiService: ApiService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Flushing pending events")
        val pendingEvents = try {
            pendingEventDao.getAllPendingEvents()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching pending events")
            return Result.failure()
        }

        if (pendingEvents.isEmpty()) {
            Timber.d("No pending events to flush")
            return Result.success()
        }

        var allSuccessful = true

        for (event in pendingEvents) {
            val request = AppEventRequest(
                eventType = event.eventType,
                occurredAt = event.occurredAt,
                sessionId = event.sessionId,
                clientEventId = event.clientEventId
            )

            try {
                val response = apiService.sendEvent(request)
                if (response.isSuccessful) {
                    pendingEventDao.deleteByClientEventId(event.clientEventId)
                    Timber.d("Successfully flushed event: ${event.clientEventId}")
                } else {
                    Timber.w("Failed to flush event: ${event.clientEventId}, status: ${response.code()}")
                    allSuccessful = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending event: ${event.clientEventId}")
                allSuccessful = false
            }
        }

        return if (allSuccessful) {
            Result.success()
        } else {
            // If it failed, WorkManager will retry based on backoff policy
            Result.retry()
        }
    }
}
