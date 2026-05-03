package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HourlyWeatherAdapter(private val hourlyData: List<HourlyWeather>) :
    RecyclerView.Adapter<HourlyWeatherAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val tempTextView: TextView = itemView.findViewById(R.id.tempTextView)
        val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_weather, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val currentItem = hourlyData[position]
        holder.timeTextView.text = currentItem.time
        holder.tempTextView.text = currentItem.temperature
        holder.iconImageView.setImageResource(currentItem.icon)
    }

    override fun getItemCount() = hourlyData.size
}