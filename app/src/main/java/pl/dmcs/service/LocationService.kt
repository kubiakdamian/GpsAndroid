package pl.dmcs.service

import android.os.Build
import com.google.android.gms.location.DetectedActivity
import pl.dmcs.model.Location
import pl.dmcs.utils.GpsVars

class LocationService {

    fun createLocation(
        location: android.location.Location,
        batteryPercentage: Int
    ): Location {
        return Location(
            location.accuracy,
            location.altitude,
            location.bearing,
            location.latitude,
            location.longitude,
            location.provider,
            location.speed,
            0,
            System.currentTimeMillis(),
            GpsVars.MOCK_SERIAL, //Setting random uuid as getting serial number requires dangerous permission READ_PHONE_STATE.
            "",
            "Android",
            Build.VERSION.SDK_INT,
            batteryPercentage
        )
    }

    fun getTransitionNameFromType(type: Int): String {
        when (type) {
            DetectedActivity.IN_VEHICLE -> {
                return "IN_VEHICLE"
            }
            DetectedActivity.ON_BICYCLE -> {
                return "ON_BICYCLE"
            }
            DetectedActivity.ON_FOOT -> {
                return "ON_FOOT"
            }
            DetectedActivity.RUNNING -> {
                return "RUNNING"
            }
            DetectedActivity.STILL -> {
                return "STILL"
            }
            DetectedActivity.TILTING -> {
                return "TILTING"
            }
            DetectedActivity.WALKING -> {
                return "WALKING"
            }
            DetectedActivity.UNKNOWN -> {
                return "UNKNOWN"
            }
        }

        return "UNKNOWN"
    }
}