package ca.doublenom.pixnails

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// Source : https://martian.ventures/mantra/insights/schedule-local-notifications-on-android/
class OneTimeScheduleWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon_pixnails)
            .setContentTitle("Pixnails")
            .setContentText("You reached your shells cap")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }

        return Result.success()
    }

    companion object {
        fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = CHANNEL_ID
            val descriptionText = CHANNEL_DESC
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        fun scheduleOneTimeNotification(context: Context, initialDelay: Long) {
            val work =
                OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .addTag(WORK_TAG)
                    .build()

            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
            WorkManager.getInstance(context).enqueue(work)
        }


        const val CHANNEL_ID = "notication_money_cap"
        private const val CHANNEL_DESC = "Money cap alert"
        private const val WORK_TAG = "money_limit_reminder"

    }

}