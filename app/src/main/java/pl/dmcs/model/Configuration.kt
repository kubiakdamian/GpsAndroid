package pl.dmcs.model

import java.io.Serializable

data class Configuration(
    val name: String,
    val token: String,
    val trackedObjectId: String,
    val positionIntervalInMinutes: Int
) : Serializable