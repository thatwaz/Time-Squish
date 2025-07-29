//package com.thatwaz.timesquish.util
//
//import android.content.Context
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.longPreferencesKey
package com.thatwaz.timesquish.util

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val REMINDER_HOURS = longPreferencesKey("reminder_hours")
        val HOURLY_PAY = doublePreferencesKey("hourly_pay")
    }

    // Reminder hours flow
    val reminderHoursFlow: Flow<Long> = context.dataStore.data
        .map { prefs -> prefs[REMINDER_HOURS] ?: 2L }

    // Hourly pay flow
    val hourlyPayFlow: Flow<Double> = context.dataStore.data
        .map { prefs -> prefs[HOURLY_PAY] ?: 0.0 }

    suspend fun setReminderHours(hours: Long) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_HOURS] = hours
        }
    }

    suspend fun setHourlyPay(rate: Double) {
        context.dataStore.edit { prefs ->
            prefs[HOURLY_PAY] = rate
        }
    }
}




//import androidx.datastore.preferences.preferencesDataStore
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//
//// This is the DataStore delegate
//val Context.dataStore by preferencesDataStore(name = "user_preferences")
//
//class UserPreferences(private val context: Context) {
//    companion object {
//        val REMINDER_HOURS = longPreferencesKey("reminder_hours")
//    }
//
//    // Flow to observe changes
//    val reminderHoursFlow: Flow<Long> = context.dataStore.data
//        .map { preferences ->
//            preferences[REMINDER_HOURS] ?: 2L // Default to 2 hours
//        }
//
//    suspend fun setReminderHours(hours: Long) {
//        context.dataStore.edit { prefs ->
//            prefs[REMINDER_HOURS] = hours
//        }
//    }
//}
