package com.thatwaz.timesquish.workers

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.thatwaz.timesquish.R
import com.thatwaz.timesquish.util.NotificationHelper

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ReminderDebug", "ReminderWorker started")

        NotificationHelper.showNotification(
            applicationContext,
            title = "Time Squish Reminder",
            message = "You are still clocked in!"
        )

        return Result.success()
    }



}

