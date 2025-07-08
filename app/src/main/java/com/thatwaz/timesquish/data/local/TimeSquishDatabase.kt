package com.thatwaz.timesquish.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TimeEntry::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimeSquishDatabase : RoomDatabase() {
    abstract fun timeEntryDao(): TimeEntryDao
}
