package com.example.weatherapp

data class HourlyWeather(
    val icon: Int, // or String if you're using icon URLs
    val temperature: String,
    val time: String
)