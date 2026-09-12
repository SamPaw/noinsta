package `in`.platesight.noinsta.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.platesight.noinsta.data.local.dao.PendingEventDao
import `in`.platesight.noinsta.data.local.entity.PendingEvent

@Database(entities = [PendingEvent::class], version = 1, exportSchema = false)
abstract class NoInstaDatabase : RoomDatabase() {
    abstract fun pendingEventDao(): PendingEventDao
}
