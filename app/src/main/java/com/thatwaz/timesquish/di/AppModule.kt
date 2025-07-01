package com.thatwaz.timesquish.di

import android.app.Application
import androidx.room.Room
import com.thatwaz.timesquish.data.local.TimeEntryDao
import com.thatwaz.timesquish.data.local.TimeSquishDatabase
import com.thatwaz.timesquish.data.repository.TimeEntryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideDatabase(app: Application): TimeSquishDatabase {
        return Room.databaseBuilder(
            app,
            TimeSquishDatabase::class.java,
            "time_squish_db"
        ).build()
    }


    @Provides
    fun provideTimeEntryDao(db: TimeSquishDatabase): TimeEntryDao {
        return db.timeEntryDao()
    }

    @Provides
    @Singleton
    fun provideRepository(dao: TimeEntryDao): TimeEntryRepository {
        return TimeEntryRepository(dao)
    }
}
