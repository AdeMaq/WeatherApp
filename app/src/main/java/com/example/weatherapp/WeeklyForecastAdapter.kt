package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeeklyForecastAdapter(private var weeklyData: List<WeeklyForecast>) :
    RecyclerView.Adapter<WeeklyForecastAdapter.WeeklyForecastViewHolder>() {

    class WeeklyForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayName: TextView = itemView.findViewById(R.id.dayName)
        val date: TextView = itemView.findViewById(R.id.date)
        val weatherIcon: ImageView = itemView.findViewById(R.id.weatherIcon)
        val minTemp: TextView = itemView.findViewById(R.id.minTemp)
        val maxTemp: TextView = itemView.findViewById(R.id.maxTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weekly_forecast, parent, false)
        return WeeklyForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyForecastViewHolder, position: Int) {
        val forecast = weeklyData[position]

        holder.dayName.text = forecast.day
        holder.date.text = forecast.date
        holder.weatherIcon.setImageResource(forecast.icon)
        holder.minTemp.text = forecast.minTemp
        holder.maxTemp.text = forecast.maxTemp
    }

    override fun getItemCount(): Int = weeklyData.size

    fun updateData(newData: List<WeeklyForecast>) {
        weeklyData = newData
        notifyDataSetChanged()
    }
}

data class WeeklyForecast(
    val day: String,
    val date: String,
    val minTemp: String,
    val maxTemp: String,
    val icon: Int
)