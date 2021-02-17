package com.swtecnn.sprinkler.view.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.swtecnn.sprinkler.R
import com.swtecnn.sprinkler.view.models.Location

class LocationAdapter(private val context: Context, private val locations: List<Location>): RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    class LocationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val locationName: TextView = itemView.findViewById(R.id.location)
        val indicator: RadioButton = itemView.findViewById(R.id.indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder =
        LocationViewHolder(
            inflater.inflate(R.layout.location_elem, parent, false)
        )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val thisLocation = locations[position]
        holder.locationName.text = thisLocation.name
        if(thisLocation.active) {
            holder.locationName.setTextColor(context.getColor(R.color.active_location))
            holder.indicator.isChecked = true
        }
        holder.locationName.contentDescription = thisLocation.name + "is not scheduled for tomorrow."
        holder.checkBox.contentDescription = thisLocation.name + " checkbox. Check to schedule this location"
        if(holder.indicator.isChecked) {
            holder.locationName.contentDescription = holder.locationName.contentDescription as String + " This location is being watered now."
        }
        holder.checkBox.setOnCheckedChangeListener {_, isChecked: Boolean ->
            if(isChecked) {
                holder.locationName.contentDescription = thisLocation.name + " is scheduled for tomorrow"
            } else {
                holder.locationName.contentDescription = thisLocation.name + " is not scheduled for tomorrow"
            }
        }
    }

    override fun getItemCount(): Int = locations.size

}