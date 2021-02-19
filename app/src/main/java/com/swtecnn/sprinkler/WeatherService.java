package com.swtecnn.sprinkler;

import android.util.Log;

import com.swtecnn.sprinkler.api.RetrofitClient;
import com.swtecnn.sprinkler.api.model.CurrentWeatherForecast;
import com.swtecnn.sprinkler.api.model.WeatherForecast;

import java.io.IOException;

import retrofit2.Response;

public class WeatherService {

    private final long ONE_HOUR_MILLIS = 3600000L;

    private final Thread currentWeatherThread;
    private final Thread forecastThread;

    public WeatherService(WeatherUICallback callback) {
        currentWeatherThread = new Thread(() -> {
            while(! Thread.interrupted()) {
                Response<CurrentWeatherForecast> response;
                try {
                    response = RetrofitClient.INSTANCE.getCurrentWeather().execute();
                } catch (IOException e) {
                    Log.e("CurrentWeatherThread",
                            "Could not fetch current weather, reason: " + e.getMessage());
                    break;
                }
                if (!response.isSuccessful()) {
                    Log.e("CurrentWeatherThread",
                            "Could not fetch current weather, reason: " + response.errorBody());
                }
                assert (response.body() != null);
                if(Thread.interrupted()) {
                    break;
                }
                callback.updateCurrentWeather(response.body().getWeather());
                try {
                    Thread.sleep(ONE_HOUR_MILLIS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        forecastThread = new Thread(() -> {
            while(! Thread.interrupted()) {
                Response<WeatherForecast> response;
                try {
                    response = RetrofitClient.INSTANCE.getWeatherForecast().execute();
                } catch (IOException e) {
                    Log.e("WeatherForecastThread",
                            "Could not fetch current weather, reason: " + e.getMessage());
                    break;
                }
                if (!response.isSuccessful()) {
                    Log.e("WeatherForecastThread",
                            "Could not fetch current weather, reason: " + response.errorBody());
                }
                assert (response.body() != null);
                if(Thread.interrupted()) {
                    break;
                }
                callback.updateForecast(response.body());
                try {
                    Thread.sleep(ONE_HOUR_MILLIS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        currentWeatherThread.start();
        forecastThread.start();
    }

    public void stop() {
        currentWeatherThread.interrupt();
        forecastThread.interrupt();
    }

    public void pause() {
        stop();
    }

    public void restart() {
        if(! currentWeatherThread.isAlive()) {
            currentWeatherThread.start();
        }

        if(! forecastThread.isAlive()) {
            forecastThread.start();
        }
    }

}
