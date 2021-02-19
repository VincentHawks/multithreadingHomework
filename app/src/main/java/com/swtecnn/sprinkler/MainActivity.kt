package com.swtecnn.sprinkler

import android.content.res.Configuration
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.children
import androidx.recyclerview.widget.AsyncDifferConfig
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
import java.util.concurrent.Executors
import kotlin.math.floor

class MainActivity : AppCompatActivity(), WeatherUICallback {

    private lateinit var sprinklerSwitcher: ImageSwitcher
    private lateinit var forecastView: RecyclerView
    private lateinit var locationView: RecyclerView
    @Volatile private lateinit var tempValue: TextView
    @Volatile private lateinit var humidValue: TextView
    private var sprinklerOnline = true

    private lateinit var forecastTask: ForecastAsyncTask
    private lateinit var currentTask: CurWthrAsyncTask

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

        forecastTask = ForecastAsyncTask()
        currentTask = CurWthrAsyncTask()
        forecastTask.executeOnExecutor(Executors.newSingleThreadExecutor(), this)
        currentTask.executeOnExecutor(Executors.newSingleThreadExecutor(), this)

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

    override fun updateCurrentWeather(weather: CurrentWeather) {
        tempValue.text = "${floor(weather.temp.toDouble()).toInt()}°"
        humidValue.text = "${weather.humidity}%"
    }

    override fun updateWeatherForecast(forecast: WeatherForecast) {
        val digestedForecast: MutableList<Forecast> = mutableListOf()
        for(f in forecast.daily) {
            digestedForecast.add(fromDailyForecast(f))
        }
        forecastView.adapter = ForecastAdapter(this, digestedForecast)
        forecastView.adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        forecastTask.cancel(true)
        currentTask.cancel(true)
    }
}