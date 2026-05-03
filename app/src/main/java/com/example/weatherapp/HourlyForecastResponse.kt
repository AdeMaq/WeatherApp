package com.example.weatherapp

data class HourlyForecastResponse(
    val list: List<HourlyForecastItem>
)

data class HourlyForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)
