package pl.dmcs

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.dmcs.api.RestApi
import pl.dmcs.model.Configuration
import pl.dmcs.utils.ToastCaller
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private val restApi = RestApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    fun switchToTracking(view: View?) {
        val intent = Intent(this, GpsActivity::class.java)
        val configId = configIdInput.text.toString()

        restApi.getConfiguration(configId)
            .enqueue(object : Callback<Configuration> {
                override fun onFailure(
                    call: Call<Configuration>?,
                    t: Throwable?
                ) {
                    ToastCaller.call(
                        this@MainActivity,
                        "Something went wrong. Try again later."
                    )
                }

                override fun onResponse(
                    call: Call<Configuration>?,
                    response: Response<Configuration>?
                ) {
                    if (response!!.code() != 200) {
                        ToastCaller.call(
                            this@MainActivity,
                            "Couldn't find configuration for given ID."
                        )
                    } else {
                        intent.putExtra("configuration", response.body())
                        startActivity(intent)
                    }
                }
            })
    }
}
