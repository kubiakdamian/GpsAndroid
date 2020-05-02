package pl.dmcs

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import pl.dmcs.api.RestApi
import pl.dmcs.model.Configuration
import pl.dmcs.model.Location
import pl.dmcs.model.Transition
import pl.dmcs.service.BackgroundActivityService
import pl.dmcs.service.LocationService
import pl.dmcs.service.PermissionsService
import pl.dmcs.utils.GpsVars
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GpsActivity : AppCompatActivity() {

    private val restApi = RestApi()
    private val locationService = LocationService()
    private var permissionsService =
        PermissionsService(this, this)

    private lateinit var configuration: Configuration
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var transitionName: String
    private lateinit var nameTextView: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var listView: ListView
    private lateinit var logs: MutableList<String>
    private lateinit var transitionList: MutableList<Transition>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var locationListener: LocationListener

    private var minutesMillis: Long = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track)
        supportActionBar?.hide()
        configuration = intent.getSerializableExtra("configuration") as Configuration
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        checkPermissions()
        initApp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initApp()
                addValueToLogs("Location permission granted.")
            } else {
                addValueToLogs("Location permission not granted.")
            }
            102 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initApp()
                addValueToLogs("Internet permission granted.")
            } else {
                addValueToLogs("Internet permission not granted.")
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(GpsVars.BROADCAST_DETECTED_ACTIVITY)
        )
        addValueToLogs("Registered : " + GpsVars.BROADCAST_DETECTED_ACTIVITY)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        addValueToLogs("Unregistered : " + GpsVars.BROADCAST_DETECTED_ACTIVITY)
    }

    fun clearLog(view: View?) {
        if (logs.size > 0) {
            logs.clear()
            adapter.notifyDataSetChanged()
        }
    }

    fun stopTracking(view: View?) {
        locationManager.removeUpdates(locationListener)
        stopTrackingActivity()
        addValueToLogs("TRACKING STOPPED")
    }


    fun changeConfiguration(view: View?) {
        stopTracking(view)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun sendLocation(location: Location) {
        restApi.postLocation(location)
            .enqueue(object : Callback<Void> {
                override fun onFailure(
                    call: Call<Void>?,
                    t: Throwable?
                ) {
                    addValueToLogs("Problem with sending location")
                }

                override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                    addValueToLogs("Successful sending location")
                }
            })
    }

    private fun getBatteryPercentage(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun checkPermissions() {
        permissionsService.requestForLocationPermission()
        permissionsService.requestForInternetPermission()
    }

    @SuppressLint("MissingPermission")
    private fun initApp() {
        setValues()
        startTrackingActivity()
        locationListener = initLocationListener()

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10,
            0f,
            locationListener
        )

        broadcastReceiver = initBroadcastReceiver()
    }

    private fun setValues() {
        logs = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logs)

        listView = findViewById(R.id.logsList)
        listView.adapter = adapter

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            permissionsService.checkGpsConnection()
        }

        nameTextView = findViewById(R.id.nameTextView)
        nameTextView.text = configuration.name

        minutesMillis = (configuration.positionIntervalInMinutes * 60000).toLong()
        transitionList = ArrayList()
    }

    private fun startTrackingActivity() {
        val intent =
            Intent(this@GpsActivity, BackgroundActivityService::class.java)
        intent.putExtra("detectionInMillis", minutesMillis)
        logs.add(
            getTime() + " Started service: " + BackgroundActivityService::class.java.name
        )
        startService(intent)
    }

    private fun stopTrackingActivity() {
        val intent =
            Intent(this@GpsActivity, BackgroundActivityService::class.java)
        logs.add(
            getTime() + " Stopped service: " + BackgroundActivityService::class.java.name
        )
        stopService(intent)
    }

    private fun getTime(): String? {
        val date: Date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH)

        return dateFormat.format(date)
    }

    private fun handleTransition(type: Int, confidence: Int) {
        transitionName = locationService.getTransitionNameFromType(type)
        if (confidence > GpsVars.CONFIDENCE) {
            addTransitionToLog(transitionName)
        }
    }

    private fun addTransitionToLog(newTransitionName: String?) {
        if (transitionList.size > 0) {
            val (previousTransitionName) = transitionList[transitionList.size - 1]
            if (previousTransitionName != newTransitionName) {
                addExitTransition(previousTransitionName)
                addEnterTransition(newTransitionName!!)
            }
        } else {
            addEnterTransition(newTransitionName!!)
        }
    }

    private fun addExitTransition(transitionName: String) {
        transitionList.add(Transition(transitionName, "EXIT"))
        addValueToLogs(" Transition: $transitionName (EXIT) ")
    }

    private fun addEnterTransition(transitionName: String) {
        transitionList.add(Transition(transitionName, "ENTER"))
        addValueToLogs(" Transition: $transitionName (ENTER) ")
    }

    private fun initLocationListener(): LocationListener {
        return object : LocationListener {

            override fun onLocationChanged(location: android.location.Location?) {
                if (shouldSendLocationToServer() && !checkIfPreviousMessageIsLocationSent()) {
                    sendLocation(locationService.createLocation(location!!, getBatteryPercentage()))
                } else {
                    if (!checkIfPreviousMessageIsLocationSent()) {
                        addValueToLogs("Transition - STILL - won't send location to server.")
                    }
                }
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

            override fun onProviderEnabled(s: String) {
                if (s == LocationManager.GPS_PROVIDER) {
                    addValueToLogs("GPS ON")
                }
            }

            override fun onProviderDisabled(s: String) {
                if (s == LocationManager.GPS_PROVIDER) {
                    addValueToLogs("GPS OFF")
                }
            }
        }
    }

    private fun initBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == GpsVars.BROADCAST_DETECTED_ACTIVITY) {
                    adapter.notifyDataSetChanged()
                    val type = intent.getIntExtra("type", -1)
                    val confidence = intent.getIntExtra("confidence", 0)
                    handleTransition(type, confidence)
                }
            }
        }
    }

    private fun addValueToLogs(message: String) {
        logs.add(getTime() + " " + message)
        adapter.notifyDataSetChanged()
    }

    private fun shouldSendLocationToServer(): Boolean {
        if (transitionList.size > 0) {
            val (previousTransitionName) = transitionList[transitionList.size - 1]
            if (previousTransitionName == "STILL") {
                return false
            }
        } else {
            return true
        }

        return true
    }

    private fun checkIfPreviousMessageIsLocationSent(): Boolean {
        if (logs.size > 0) {
            val previousLog = logs[logs.size - 1]
            if (previousLog.contains("Successful sending location") || previousLog.contains("won't send location to server")) {
                return true
            }
        }

        return false
    }
}