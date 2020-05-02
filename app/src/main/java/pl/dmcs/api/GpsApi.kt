package pl.dmcs.api

import pl.dmcs.model.Configuration
import pl.dmcs.model.Location
import retrofit2.Call
import retrofit2.http.*

interface GpsApi {

    @GET("/configuration/{id}")
    fun getConfigurationForId(@Path("id") id: String): Call<Configuration>

    @POST("/location")
    @Headers("Content-type: application/json")
    fun postLocation(@Body location: Location): Call<Void>
}