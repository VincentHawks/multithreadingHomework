package com.swtecnn.sprinkler.api.model

data class ForecastAndCurrentWeather (
    val current: CurrentWeather,
    val forecast: WeatherForecast
)