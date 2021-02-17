package com.swtecnn.sprinkler

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swtecnn.sprinkler.api.RetrofitClient
import com.swtecnn.sprinkler.api.model.CurrentWeather
import com.swtecnn.sprinkler.api.model.CurrentWeatherForecast
import com.swtecnn.sprinkler.api.model.WeatherForecast
import com.swtecnn.sprinkler.view.adapters.ForecastAdapter
import com.swtecnn.sprinkler.view.adapters.LocationAdapter
import com.swtecnn.sprinkler.view.models.Forecast
import com.swtecnn.sprinkler.view.models.Location
import com.swtecnn.sprinkler.view.models.fromDailyForecast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.floor

const val ONE_HOUR_MILLIS = 3600000L

class MainActivity : AppCompatActivity() {

    private lateinit var sprinklerSwitcher: ImageSwitcher
    private lateinit var forecastView: RecyclerView
    private lateinit var locationView: RecyclerView
    @Volatile private lateinit var tempValue: TextView
    @Volatile private lateinit var humidValue: TextView
    private var sprinklerOnline = true

    @Volatile var forecasts: MutableList<Forecast> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val locationLayoutManager = LinearLayoutManager(this)
        forecastView = findViewById(R.id.forecast)
        locationView = findViewById(R.id.locations)
        tempValue = findViewById(R.id.tempValue)
        humidValue = findViewById(R.id.humidValue)

        locationView.layoutManager = locationLayoutManager
        forecastView.setHasFixedSize(true)
        locationView.setHasFixedSize(true)
        locationView.addItemDecoration(DividerItemDecoration(locationView.context, locationView.layoutManager!!.layoutDirection))


        forecastView.adapter =
            ForecastAdapter(this, forecasts)

        val handlerThread = HandlerThread("WeatherUpdater").run {

            RetrofitClient.getWeatherForecast().enqueue(object:
                Callback<WeatherForecast> {
                override fun onFailure(call: Call<WeatherForecast>, t: Throwable) {
                    Log.e("WeatherThread", "Failed to fetch weather data")
                }

                override fun onResponse(
                    call: Call<WeatherForecast>,
                    response: Response<WeatherForecast>
                ) {
                    val digestedForecast: MutableList<Forecast> = mutableListOf()
                    val rawForecast: WeatherForecast = (response.body() as WeatherForecast)
                    for (forecast in rawForecast.daily) {
                        digestedForecast.add(fromDailyForecast(forecast))
                    }
                    runOnUiThread {
                        (forecastView.adapter!! as ForecastAdapter).forecast.clear()
                        (forecastView.adapter!! as ForecastAdapter).forecast.addAll(digestedForecast)
                        forecastView.adapter!!.notifyDataSetChanged()
                    }
                }
            })


            RetrofitClient.getCurrentWeather().enqueue(object: Callback<CurrentWeatherForecast> {
                override fun onFailure(call: Call<CurrentWeatherForecast>, t: Throwable) {
                    Log.e("WeatherThread", "Unable to fetch current weather")
                }

                override fun onResponse(
                    call: Call<CurrentWeatherForecast>,
                    response: Response<CurrentWeatherForecast>
                ) {
                    val rawCurrentWeather = (response.body() as CurrentWeatherForecast).weather
                    tempValue.text = "${floor(rawCurrentWeather.temp.toDouble()).toInt()}°"
                    humidValue.text = "${rawCurrentWeather.humidity}%"
                }
            })
        }

        val locations = listOf(
            Location("Backyard"),
            Location("Back Patio"),
            Location("Front Yard", active = true),
            Location("Garden"),
            Location("Porch")
        )
        locationView.adapter =
            LocationAdapter(this, locations)
        locationView.addItemDecoration(DividerItemDecoration(
            locationView.context,
            locationLayoutManager.orientation
        ))

        val forecastGuidelinePercent: Float = when(resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 0.33f
            Configuration.ORIENTATION_LANDSCAPE -> 0.25f
            else -> 0.25f
        }
        val forecastGuideline: Guideline = findViewById(R.id.forecastGuideline)
        forecastGuideline.setGuidelinePercent(forecastGuidelinePercent)

    }

    fun onSprinklerClick(view: View) {
        findViewById<ImageSwitcher>(R.id.sprinklerIcon).showNext()
        sprinklerOnline = !sprinklerOnline
        if(!sprinklerOnline) {
            for (component in locationView.children) {
                component.findViewById<CheckBox>(R.id.checkBox).isChecked = false
            }
        }
    }
}