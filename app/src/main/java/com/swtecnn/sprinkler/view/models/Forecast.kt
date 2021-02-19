package com.swtecnn.sprinkler.view.models

import com.swtecnn.sprinkler.R
import com.swtecnn.sprinkler.api.model.DailyForecast
import kotlin.math.floor

fun fromDailyForecast(forecast: DailyForecast): Forecast = Forecast(
    forecast.getDate(),
    "${floor(forecast.temp.day).toInt().toString()}°",
    R.drawable.cloudy // TODO replace with actual image fetching
)

data class Forecast(
    val datestamp: String,
    val temperature: String,
    val icon: Int
)