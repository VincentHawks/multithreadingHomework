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
import kotlin.math.floor

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

        class ForecastAsyncTask: AsyncTask<Void, Void, WeatherForecast?>() {

            override fun doInBackground(vararg params: Void): WeatherForecast? {
                val weatherForecastResponse = RetrofitClient.getWeatherForecast().execute()
                if(!weatherForecastResponse.isSuccessful) {
                    Log.e("WeatherForecastThread",
                    "Weather forecast couldn't be fetched, reason: ${weatherForecastResponse.errorBody()}")
                    return null
                }
                return weatherForecastResponse.body()
            }

            override fun onPostExecute(result: WeatherForecast?) {
                super.onPostExecute(result)
                if(result == null) {
                    return
                }
                val digestedForecast: MutableList<Forecast> = mutableListOf()
                for(forecast in result.daily) {
                    digestedForecast.add(fromDailyForecast(forecast))
                }
                (forecastView.adapter!! as ForecastAdapter).forecast.clear()
                (forecastView.adapter!! as ForecastAdapter).forecast.addAll(digestedForecast)
                forecastView.adapter!!.notifyDataSetChanged()
            }

        }

        class CurWthrAsyncTask: AsyncTask<Void, Void, CurrentWeather?>() {

            override fun doInBackground(vararg params: Void?): CurrentWeather? {
                val currentWeatherResponse = RetrofitClient.getCurrentWeather().execute()
                if(!currentWeatherResponse.isSuccessful) {
                    Log.e("CurrentWeatherThread",
                    "Current weather couldn't be fetched, reason: ${currentWeatherResponse.errorBody()}")
                    return null
                }
                return currentWeatherResponse.body()?.weather
            }

            override fun onPostExecute(result: CurrentWeather?) {
                super.onPostExecute(result)
                if(result == null) {
                    return
                }
                tempValue.text = "${floor(result.temp.toDouble()).toInt()}°"
                humidValue.text = "${result.humidity}%"
            }

        }

        val forecastTask = ForecastAsyncTask()
        val currentTask = CurWthrAsyncTask()
        forecastTask.execute(); currentTask.execute()

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