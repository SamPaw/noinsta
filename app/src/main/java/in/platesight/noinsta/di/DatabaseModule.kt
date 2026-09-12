package `in`.platesight.noinsta.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.platesight.noinsta.data.local.NoInstaDatabase
import `in`.platesight.noinsta.data.local.dao.PendingEventDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoInstaDatabase {
        return Room.databaseBuilder(
            context,
            NoInstaDatabase::class.java,
            "noinsta.db"
        ).build()
    }

    @Provides
    fun providePendingEventDao(database: NoInstaDatabase): PendingEventDao {
        return database.pendingEventDao()
    }
}
