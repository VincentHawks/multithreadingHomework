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
import retrofit2.Retrofit
import kotlin.math.floor

const val ONE_HOUR_MILLIS = 3600000L

class MainActivity : AppCompatActivity() {

    private lateinit var sprinklerSwitcher: ImageSwitcher
    private lateinit var forecastView: RecyclerView
    private lateinit var locationView: RecyclerView
    private lateinit var tempValue: TextView
    private lateinit var humidValue: TextView
    private var sprinklerOnline = true

    private lateinit var currentThread: Thread
    private lateinit var forecastThread: Thread
    private val handler = Handler()

    var forecasts: MutableList<Forecast> = mutableListOf()

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

        currentThread = Thread {
            while(! Thread.interrupted()) {
                val result = RetrofitClient.getCurrentWeather().execute()
                if(! result.isSuccessful) {
                    Log.e("CurrentWeatherThread",
                        "Could not fetch current weather, reason: ${result.errorBody()}")
                } else {
                    if (Thread.interrupted()) {
                        break
                    }
                    val currentWeather = result.body()!!.weather
                    handler.post {
                        tempValue.text = "${floor(currentWeather.temp).toInt().toString()}°"
                        humidValue.text = "${currentWeather.humidity}%"
                    }
                }
                try {
                    Thread.sleep(ONE_HOUR_MILLIS)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        forecastThread = Thread {
            while(! Thread.interrupted()) {
                val result = RetrofitClient.getWeatherForecast().execute()
                if(! result.isSuccessful) {
                    Log.e("WeatherForecastThread",
                    "Could not fetch weather forecast, reason: ${result.errorBody()}")
                } else {
                    val forecast: WeatherForecast = result.body()!!
                    val digestedForecast = mutableListOf<Forecast>()
                    for(f in forecast.daily) {
                        digestedForecast.add(fromDailyForecast(f))
                    }
                    handler.post {
                        forecastView.adapter = ForecastAdapter(this@MainActivity, digestedForecast)
                        forecastView.adapter!!.notifyDataSetChanged()
                    }
                }
                try {
                    Thread.sleep(ONE_HOUR_MILLIS)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        currentThread.start()
        forecastThread.start()

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

    override fun onDestroy() {
        forecastThread.interrupt()
        currentThread.interrupt()
        super.onDestroy()
    }
}