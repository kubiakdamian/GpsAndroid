package pl.dmcs.utils

import java.util.*

class GpsVars {
    companion object {
        const val BROADCAST_DETECTED_ACTIVITY = "activity_intent"

        const val CONFIDENCE = 50 // Confidence set to low value, to show acting of an app. In serious case it should be higher

        val MOCK_SERIAL = UUID.randomUUID().toString()
    }
}