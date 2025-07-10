package com.thatwaz.timesquish.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thatwaz.timesquish.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showNotification(
            context,
            title = "Time Squish Reminder",
            message = "You are still clocked in!"
        )
    }
}
