package com.example.weatherapp

data class AirQualityResponse(
    val list: List<AQData>
)

data class AQData(
    val main: AQIMain
)

data class AQIMain(
    val aqi: Int
)

