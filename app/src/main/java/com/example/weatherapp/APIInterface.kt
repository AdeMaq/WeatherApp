package com.example.weatherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("weather")
    fun getWeatherDta(
        @Query("q") city:String,
        @Query("appid") appid :String,
        @Query("units") units:String
    ): Call<weatherApp>

    @GET("forecast")
    fun getHourlyForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("cnt") count: Int = 24
    ): Call<HourlyForecastResponse>

    @GET("air_pollution")
    fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String
    ): Call<AirQualityResponse>

//    @GET("forecast/daily")
//    fun getWeeklyForecast(
//        @Query("q") city: String,
//        @Query("appid") apiKey: String,
//        @Query("units") units: String,
//        @Query("cnt") count: Int = 15
//    ): Call<WeeklyForecastResponse>

    @GET("forecast")
    fun getWeeklyForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<WeeklyForecastResponse>



}