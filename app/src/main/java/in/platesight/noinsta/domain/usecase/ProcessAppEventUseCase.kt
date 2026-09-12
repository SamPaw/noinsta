package `in`.platesight.noinsta.domain.usecase

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import `in`.platesight.noinsta.data.local.dao.PendingEventDao
import `in`.platesight.noinsta.data.local.entity.PendingEvent
import `in`.platesight.noinsta.data.remote.ApiService
import `in`.platesight.noinsta.data.remote.model.AppEventRequest
import `in`.platesight.noinsta.domain.model.AppEventType
import `in`.platesight.noinsta.service.FlushPendingEventsWorker
import timber.log.Timber
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class ProcessAppEventUseCase @Inject constructor(
    private val pendingEventDao: PendingEventDao,
    private val apiService: ApiService,
    private val workManager: WorkManager
) {
    suspend operator fun invoke(type: AppEventType, sessionId: String) {
        val clientEventId = "evt_${UUID.randomUUID()}"
        val occurredAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        
        val event = PendingEvent(
            clientEventId = clientEventId,
            eventType = type.value,
            sessionId = sessionId,
            occurredAt = occurredAt,
            createdAt = System.currentTimeMillis()
        )

        // 1. Write to local DB immediately
        pendingEventDao.insertEvent(event)

        // 2. Attempt immediate transmission
        val request = AppEventRequest(
            eventType = type.value,
            occurredAt = occurredAt,
            sessionId = sessionId,
            clientEventId = clientEventId
        )

        try {
            val response = apiService.sendEvent(request)
            if (response.isSuccessful) {
                // 3. Delete from DB if successful
                pendingEventDao.deleteByClientEventId(clientEventId)
                Timber.d("Event sent successfully: $clientEventId")
            } else {
                Timber.w("Failed to send event immediately, enqueuing worker: ${response.code()}")
                enqueueWorker()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending event immediately, enqueuing worker")
            enqueueWorker()
        }
    }

    private fun enqueueWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<FlushPendingEventsWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(workRequest)
    }
}
