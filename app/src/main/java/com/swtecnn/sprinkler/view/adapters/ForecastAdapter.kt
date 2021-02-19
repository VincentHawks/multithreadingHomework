package com.swtecnn.sprinkler.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swtecnn.sprinkler.R
import com.swtecnn.sprinkler.view.models.Forecast

class ForecastAdapter(context: Context, var forecast: MutableList<Forecast>): RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    class ForecastViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var datestamp: TextView = itemView.findViewById(R.id.datestamp)
        var forecastTemp: TextView = itemView.findViewById(R.id.forecastTemp)
        var weatherIcon: ImageView = itemView.findViewById(R.id.weatherIcon)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder =
        ForecastViewHolder(
            inflater.inflate(R.layout.forecast_elem, parent, false)
        )

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val thisForecast: Forecast = forecast[position]
        holder.datestamp.text = thisForecast.datestamp
        holder.forecastTemp.text = thisForecast.temperature
        holder.weatherIcon.setImageResource(thisForecast.icon)
    }

    override fun getItemCount(): Int = forecast.size
}