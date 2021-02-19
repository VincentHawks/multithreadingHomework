package com.swtecnn.sprinkler

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageSwitcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swtecnn.sprinkler.api.RetrofitClient
import com.swtecnn.sprinkler.api.model.CurrentWeatherForecast
import com.swtecnn.sprinkler.api.model.WeatherForecast
import com.swtecnn.sprinkler.view.adapters.ForecastAdapter
import com.swtecnn.sprinkler.view.adapters.LocationAdapter
import com.swtecnn.sprinkler.view.models.Forecast
import com.swtecnn.sprinkler.view.models.Location
import com.swtecnn.sprinkler.view.models.fromDailyForecast
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    private lateinit var sprinklerSwitcher: ImageSwitcher
    private lateinit var forecastView: RecyclerView
    private lateinit var locationView: RecyclerView
    @Volatile private lateinit var tempValue: TextView
    @Volatile private lateinit var humidValue: TextView
    private var sprinklerOnline = true

    private var scope = CoroutineScope(Job())

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

        //val weatherService = WeatherService(this)

        scope.launch {
            val result = RetrofitClient.getCurrentWeather()
            runOnUiThread {
                updateCurrentWeather(result)
            }
        }

        scope.launch {
            val result = RetrofitClient.getWeatherForecast()
            runOnUiThread {
                updateForecast(result)
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

    private fun updateCurrentWeather(forecast: CurrentWeatherForecast) {
        val weather = forecast.weather
        tempValue.text = "${floor(weather.temp).toInt().toString()}°"
        humidValue.text = "${weather.humidity}%"
    }

    private fun updateForecast(forecast: WeatherForecast) {
        val digestedForecast = mutableListOf<Forecast>()
        for(f in forecast.daily) {
            digestedForecast.add(fromDailyForecast(f))
        }
        forecastView.adapter = ForecastAdapter(this@MainActivity, digestedForecast)
        forecastView.adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}