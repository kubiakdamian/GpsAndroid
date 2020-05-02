package pl.dmcs.api

import pl.dmcs.model.Configuration
import pl.dmcs.model.Location
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RestApi {

    private val gpsApi: GpsApi
    private val baseUrl = "http://dmcs-gps.herokuapp.com"

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        gpsApi = retrofit.create(GpsApi::class.java)
    }

    fun getConfiguration(id: String): Call<Configuration> {
        return gpsApi.getConfigurationForId(id)
    }

    fun postLocation(location: Location): Call<Void> {
        return gpsApi.postLocation(location)
    }
}