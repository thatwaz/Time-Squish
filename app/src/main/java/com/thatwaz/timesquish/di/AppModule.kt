package com.thatwaz.timesquish.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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


    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE time_entries ADD COLUMN hourlyPay REAL NOT NULL DEFAULT 0.0")
        }
    }



    @Provides
    @Singleton
    fun provideDatabase(app: Application): TimeSquishDatabase {
        return Room.databaseBuilder(
            app,
            TimeSquishDatabase::class.java,
            "time_squish_db"
        )
            .addMigrations(MIGRATION_4_5) // ✅ Apply the migration
            .build()
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
