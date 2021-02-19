package com.swtecnn.sprinkler

import android.os.AsyncTask
import android.util.Log
import com.swtecnn.sprinkler.api.RetrofitClient
import com.swtecnn.sprinkler.api.model.CurrentWeather
import com.swtecnn.sprinkler.api.model.WeatherForecast

class ForecastAsyncTask: AsyncTask<WeatherUICallback, Void, WeatherForecast?>() {

    lateinit var callback: WeatherUICallback

    override fun doInBackground(vararg params: WeatherUICallback): WeatherForecast? {
        val weatherForecastResponse = RetrofitClient.getWeatherForecast().execute()
        if(!weatherForecastResponse.isSuccessful) {
            Log.e("WeatherForecastThread",
                "Weather forecast couldn't be fetched, reason: ${weatherForecastResponse.errorBody()}")
            return null
        }
        callback = params[0]
        return weatherForecastResponse.body()
    }

    override fun onPostExecute(result: WeatherForecast?) {
        super.onPostExecute(result)
        if(result == null) {
            return
        }

        if(! isCancelled) {
            callback.updateWeatherForecast(result)
        }
    }

}

class CurWthrAsyncTask: AsyncTask<WeatherUICallback, Void, CurrentWeather?>() {

    lateinit var callback: WeatherUICallback

    override fun doInBackground(vararg params: WeatherUICallback): CurrentWeather? {
        val currentWeatherResponse = RetrofitClient.getCurrentWeather().execute()
        if(!currentWeatherResponse.isSuccessful) {
            Log.e("CurrentWeatherThread",
                "Current weather couldn't be fetched, reason: ${currentWeatherResponse.errorBody()}")
            return null
        }
        callback = params[0]
        return currentWeatherResponse.body()?.weather
    }

    override fun onPostExecute(result: CurrentWeather?) {
        super.onPostExecute(result)
        if(result == null) {
            return
        }
        if(! isCancelled) {
            callback.updateCurrentWeather(result)
        }
    }

}