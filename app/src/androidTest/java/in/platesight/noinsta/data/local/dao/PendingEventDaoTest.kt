package `in`.platesight.noinsta.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.platesight.noinsta.data.local.NoInstaDatabase
import `in`.platesight.noinsta.data.local.entity.PendingEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingEventDaoTest {

    private lateinit var database: NoInstaDatabase
    private lateinit var dao: PendingEventDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoInstaDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.pendingEventDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetEvents() = runBlocking {
        val event = PendingEvent(
            clientEventId = "evt_1",
            eventType = "instagram_open",
            sessionId = "sess_1",
            occurredAt = "2026-09-10T15:20:00Z",
            createdAt = System.currentTimeMillis()
        )
        dao.insertEvent(event)

        val events = dao.getAllPendingEvents()
        assertEquals(1, events.size)
        assertEquals("evt_1", events[0].clientEventId)
    }

    @Test
    fun deleteEvent() = runBlocking {
        val event = PendingEvent(
            clientEventId = "evt_1",
            eventType = "instagram_open",
            sessionId = "sess_1",
            occurredAt = "2026-09-10T15:20:00Z",
            createdAt = System.currentTimeMillis()
        )
        dao.insertEvent(event)
        dao.deleteByClientEventId("evt_1")

        val events = dao.getAllPendingEvents()
        assertTrue(events.isEmpty())
    }

    @Test
    fun orderingByOccurredAt() = runBlocking {
        val event1 = PendingEvent(
            clientEventId = "evt_1",
            eventType = "instagram_open",
            sessionId = "sess_1",
            occurredAt = "2026-09-10T15:20:00Z",
            createdAt = System.currentTimeMillis()
        )
        val event2 = PendingEvent(
            clientEventId = "evt_2",
            eventType = "instagram_close",
            sessionId = "sess_1",
            occurredAt = "2026-09-10T15:10:00Z", // Earlier
            createdAt = System.currentTimeMillis()
        )
        dao.insertEvent(event1)
        dao.insertEvent(event2)

        val events = dao.getAllPendingEvents()
        assertEquals(2, events.size)
        assertEquals("evt_2", events[0].clientEventId) // Order ASC
        assertEquals("evt_1", events[1].clientEventId)
    }
}
