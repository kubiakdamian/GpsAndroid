package pl.dmcs.model

data class Location(
    var acc: Float,
    val alt: Double,
    val bea: Float,
    val lat: Double,
    val lon: Double,
    val prov: String,
    val spd: Float,
    val sat: Int,
    val timestamp: Long,
    val serial: String,
    val tid: String,
    val plat: String,
    val platVersion: Int,
    val bat: Int
)