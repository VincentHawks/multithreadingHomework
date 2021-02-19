package com.swtecnn.sprinkler

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.swtecnn.sprinkler.api.RetrofitClient
import com.swtecnn.sprinkler.api.model.CurrentWeather
import com.swtecnn.sprinkler.view.models.Forecast
import com.swtecnn.sprinkler.view.models.fromDailyForecast
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

const val TAG = "WeatherLiveData"

class MainViewmodel: ViewModel() {

    var weatherForecastData = MutableLiveData<List<Forecast>>()
    var currentWeatherData = MutableLiveData<CurrentWeather>()

    var disposable = CompositeDisposable()

    init {
        disposable.addAll(
                RetrofitClient.getWeatherForecast()
                        .subscribeOn(Schedulers.io())
                        .doOnSuccess {
                            val digestedForecast = mutableListOf<Forecast>()
                            for(f in it.daily) {
                                digestedForecast.add(fromDailyForecast(f))
                            }
                            weatherForecastData.postValue(digestedForecast)
                        }
                        .doOnError {
                            Log.e(TAG,"Failed to fetch forecast data, reason: ${it.message}")
                        }
                        .subscribe(),

                RetrofitClient.getCurrentWeather()
                        .subscribeOn(Schedulers.io())
                        .doOnSuccess {
                            currentWeatherData.postValue(it.weather)
                        }
                        .doOnError {
                            Log.e(TAG, "Failed to fetch current weather, reason: ${it.message}")
                        }
                        .subscribe()
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}