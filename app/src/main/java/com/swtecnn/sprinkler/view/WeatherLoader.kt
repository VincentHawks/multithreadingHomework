package com.swtecnn.sprinkler.view

import android.content.Context
import android.util.Log
import androidx.loader.content.AsyncTaskLoader
import com.swtecnn.sprinkler.api.RetrofitClient
import com.swtecnn.sprinkler.api.model.ForecastAndCurrentWeather

class WeatherLoader(context: Context) : AsyncTaskLoader<ForecastAndCurrentWeather>(context) {

    override fun loadInBackground(): ForecastAndCurrentWeather? {
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
            return ForecastAndCurrentWeather(
                forecast = weatherForecastResponse.body()!!,
                current = currentWeatherResponse.body()!!.weather
            )
        }
        return null
    }
}