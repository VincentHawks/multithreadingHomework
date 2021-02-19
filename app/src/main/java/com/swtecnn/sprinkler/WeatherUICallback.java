package com.swtecnn.sprinkler;

import com.swtecnn.sprinkler.api.model.CurrentWeather;
import com.swtecnn.sprinkler.api.model.WeatherForecast;

import org.jetbrains.annotations.NotNull;

public interface WeatherUICallback {

    void updateCurrentWeather(@NotNull CurrentWeather weather);
    void updateWeatherForecast(@NotNull WeatherForecast forecast);

}
