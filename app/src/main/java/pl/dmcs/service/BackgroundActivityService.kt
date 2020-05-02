package pl.dmcs.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.ActivityRecognitionClient
import pl.dmcs.utils.ToastCaller


class BackgroundActivityService : Service() {

    private lateinit var intentService: Intent
    private lateinit var pendingIntent: PendingIntent
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private var detectionTime: Long = 5000 //default detection time

    private var binder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: BackgroundActivityService
            get() = this@BackgroundActivityService
    }

    override fun onCreate() {
        super.onCreate()
        activityRecognitionClient = ActivityRecognitionClient(this)
        intentService = Intent(this, DetectedActivityService::class.java)
        pendingIntent =
            PendingIntent.getService(this, 1, intentService, PendingIntent.FLAG_UPDATE_CURRENT)
        requestActivityUpdatesButtonHandler()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        detectionTime = intent.getSerializableExtra("detectionInMillis") as Long
        return START_STICKY
    }

    private fun requestActivityUpdatesButtonHandler() {
        val task = activityRecognitionClient.requestActivityUpdates(
            detectionTime,
            pendingIntent
        )

        task?.addOnSuccessListener {
            ToastCaller.call(
                this@BackgroundActivityService,
                "Successfully requested activity updates"
            )
        }

        task?.addOnFailureListener {
            ToastCaller.call(
                this@BackgroundActivityService,
                "Requesting activity updates failed to start"
            )
        }
    }

    private fun removeActivityUpdatesButtonHandler() {
        val task = activityRecognitionClient.removeActivityUpdates(
            pendingIntent
        )
        task?.addOnSuccessListener {
            ToastCaller.call(
                this@BackgroundActivityService,
                "Removed activity updates successfully!"
            )
        }

        task?.addOnFailureListener {
            ToastCaller.call(
                this@BackgroundActivityService,
                "Failed to remove activity updates!"
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivityUpdatesButtonHandler()
    }
}