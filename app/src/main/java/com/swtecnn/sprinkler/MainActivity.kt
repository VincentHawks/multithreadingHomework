package com.swtecnn.sprinkler

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.swtecnn.sprinkler.api.model.WeatherForecast
import com.swtecnn.sprinkler.view.adapters.ForecastAdapter
import com.swtecnn.sprinkler.view.adapters.LocationAdapter
import com.swtecnn.sprinkler.view.models.Forecast
import com.swtecnn.sprinkler.view.models.Location
import com.swtecnn.sprinkler.view.models.fromDailyForecast
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
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
        val executor = ThreadPoolExecutor(
            4, 8, 1, TimeUnit.HOURS, ArrayBlockingQueue<Runnable>(8)
        )

        executor.execute {
            while(!Thread.interrupted()) {
                val weatherForecastResponse = RetrofitClient.getWeatherForecast().execute()
                val currentWeatherResponse = RetrofitClient.getCurrentWeather().execute()

                if (!weatherForecastResponse.isSuccessful) {
                    Log.println(
                        Log.ERROR, "WeatherThread",
                        "Weather forecast couldn't be fetched, reason: ${weatherForecastResponse.errorBody()}"
                    )
                }

                if (!currentWeatherResponse.isSuccessful) {
                    Log.e(
                        "WeatherThread",
                        "Current weather couldn't be fetched, reason: ${currentWeatherResponse.errorBody()}"
                    )
                }

                if (weatherForecastResponse.isSuccessful && currentWeatherResponse.isSuccessful) {
                    // If fetched successfully, update the data on screen
                    val digestedForecast: MutableList<Forecast> = mutableListOf()
                    val rawForecast: WeatherForecast = weatherForecastResponse.body()!!
                    val rawCurrentWeather: CurrentWeather = currentWeatherResponse.body()!!.weather
                    for(forecast in rawForecast.daily) {
                        digestedForecast.add(fromDailyForecast(forecast))
                    }
                    if(!Thread.interrupted()) {
                        runOnUiThread {
                            (forecastView.adapter!! as ForecastAdapter).forecast.clear()
                            (forecastView.adapter!! as ForecastAdapter).forecast.addAll(digestedForecast)
                            forecastView.adapter!!.notifyDataSetChanged()
                            tempValue.text = "${floor(rawCurrentWeather.temp.toDouble()).toInt()}°"
                            humidValue.text = "${rawCurrentWeather.humidity}%"
                        }
                    } else {
                        return@execute
                    }
                }

                try {
                    Thread.sleep(ONE_HOUR_MILLIS)
                } catch (e: InterruptedException) {
                    return@execute
                }
            }
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