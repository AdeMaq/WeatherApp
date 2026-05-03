package com.example.weatherapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.ActivityObserveBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class ObserveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObserveBinding
    private lateinit var weeklyAdapter: WeeklyForecastAdapter
    private var cityName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObserveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cityName = intent.getStringExtra("cityName") ?: "lahore"

        setupRecyclerViews()
        fetchWeeklyForecast()
    }

    private fun setupRecyclerViews() {
        weeklyAdapter = WeeklyForecastAdapter(emptyList())
        binding.weeklyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ObserveActivity)
            adapter = weeklyAdapter
        }
    }

    private fun fetchWeeklyForecast() {

        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("https://api.openweathermap.org/data/2.5/").build().create(APIInterface::class.java)
        val weeklyForecastCall = retrofit.getWeeklyForecast(cityName, "fbeb38e5a71bb9df72575e80c11991c8", "metric")
        weeklyForecastCall.enqueue(object : Callback<WeeklyForecastResponse> {
            override fun onResponse(call: Call<WeeklyForecastResponse>, response: Response<WeeklyForecastResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val filteredList = response.body()!!.list
                        .filter { forecastItem -> forecastItem.dt_txt.contains("12:00:00") }
                    val weeklyData = filteredList.map { forecastItem ->
                        WeeklyForecast(
                            day = formatDay(forecastItem.dt),
                            date = formatDate(forecastItem.dt),
                            minTemp = "${forecastItem.main.temp_min.toInt()}°",
                            maxTemp = "${forecastItem.main.temp_max.toInt()}°",
                            icon = getWeatherIcon(forecastItem.weather.firstOrNull()?.main ?: "unknown")
                        )
                    }
                    weeklyAdapter.updateData(weeklyData)

                }
            }
            override fun onFailure(call: Call<WeeklyForecastResponse>, t: Throwable) {
                Log.e("ObserveActivity", "Weekly forecast API call failed: ${t.message}")
            }


        })
        val response=retrofit.getWeatherDta(cityName,"fbeb38e5a71bb9df72575e80c11991c8","metric")
        response.enqueue(object:Callback<weatherApp>{
            override fun onResponse(call: Call<weatherApp>, response: Response<weatherApp>) {
                val responseBody=response.body()
                if(response.isSuccessful && responseBody!=null)
                {
                    val sunRise = responseBody.sys.sunrise.toLong() * 1000
                    val sunSet = responseBody.sys.sunset.toLong() * 1000
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val currentTime = System.currentTimeMillis()
                    val isDayTime = currentTime in sunRise..sunSet
                    changeImagesAccordingToWeatherConditions(condition,isDayTime)
                    val tips = getTipsForWeather(condition)
                    showTipsInFrameLayout(tips)
                }
            }
            override fun onFailure(call: Call<weatherApp>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}")
            }
        })
    }
    private fun showTipsInFrameLayout(tips: List<WeatherTip>) {
        val inflater = LayoutInflater.from(this)
        val container = binding.lifestyleContainer
        val flow = binding.lifestyleFlow
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child.tag == "tip") viewsToRemove.add(child)
        }
        viewsToRemove.forEach { container.removeView(it) }
        val tipIds = mutableListOf<Int>()
        tips.forEach { tip ->
            val tipView = inflater.inflate(R.layout.item_tip_layout, container, false) as LinearLayout
            tipView.id = View.generateViewId()
            tipView.findViewById<ImageView>(R.id.tip_icon).setImageResource(tip.iconResId)
            tipView.findViewById<TextView>(R.id.tip_text).text = tip.message
            container.addView(tipView)
            tipIds.add(tipView.id)
        }

        flow.referencedIds = tipIds.toIntArray()
    }

    private fun getTipsForWeather(condition: String): List<WeatherTip> {
        return when (condition.lowercase()) {
            "clear","haze" -> listOf(
                WeatherTip(R.drawable.hydration, "Stay hydrated"),
                WeatherTip(R.drawable.sunglasses, "Use sunglasses"),
                WeatherTip(R.drawable.treatment, "Wear sunscreen"),
                WeatherTip(R.drawable.sunny, "Avoid peak sun hours"),
                WeatherTip(R.drawable.tshirt, "Light clothing recommended"),
                WeatherTip(R.drawable.mosquito, "Some mosquitos"),
                WeatherTip(R.drawable.map, "Not suitable for trip"),
                WeatherTip(R.drawable.walk, "Indoor walks recommended")
            )
            "rain", "drizzle" -> listOf(
                WeatherTip(R.drawable.umberella, "Carry an umbrella"),
                WeatherTip(R.drawable.boot, "Wear waterproof shoes"),
                WeatherTip(R.drawable.news, "Avoid flood-prone areas"),
                WeatherTip(R.drawable.car, "Drive carefully"),
                WeatherTip(R.drawable.tshirt, "Wear a raincoat"),
                WeatherTip(R.drawable.walk, "Avoid outdoor sports")
            )
            "clouds" -> listOf(
                WeatherTip(R.drawable.tshirt, "Take a light jacket"),
                WeatherTip(R.drawable.walk, "Good day for a walk"),
                WeatherTip(R.drawable.rain, "Chance of rain"),
                WeatherTip(R.drawable.walk, "Nice weather for photos"),
                WeatherTip(R.drawable.treatment, "No sunscreen needed"),
                WeatherTip(R.drawable.cloud, "Cool and comfortable")
            )
            "snow" -> listOf(
                WeatherTip(R.drawable.tshirt, "Wear warm clothes"),
                WeatherTip(R.drawable.car, "Drive carefully"),
                WeatherTip(R.drawable.boot, "Use snow boots"),
                WeatherTip(R.drawable.snow, "Avoid long exposure"),
                WeatherTip(R.drawable.snow, "Enjoy the snow!"),
                WeatherTip(R.drawable.news, "Check for delays")
            )
            "thunderstorm" -> listOf(
                WeatherTip(R.drawable.thunder, "Stay indoors"),
                WeatherTip(R.drawable.power_plug, "Unplug electronics"),
                WeatherTip(R.drawable.cloud, "Avoid trees"),
                WeatherTip(R.drawable.map, "Avoid travel"),
                WeatherTip(R.drawable.messages, "Be safe"),
                WeatherTip(R.drawable.news, "Watch weather updates")
            )
            else -> listOf(
                WeatherTip(R.drawable.news, "Check local news"),
                WeatherTip(R.drawable.messages, "Stay updated"),
                WeatherTip(R.drawable.tshirt, "Dress accordingly"),
                WeatherTip(R.drawable.map, "Stay safe"),
                WeatherTip(R.drawable.messages, "Monitor alerts"),
                WeatherTip(R.drawable.map, "Be aware of surroundings")
            )
        }
    }


    private fun changeImagesAccordingToWeatherConditions(conditions: String,isDayTime: Boolean) {
        when (conditions) {
            "Clear", "Sunny" -> {
                if (isDayTime) {
                    binding.root.setBackgroundResource(R.drawable.sunny_mountain)
                } else {
                    binding.root.setBackgroundResource(R.drawable.clear_night)
                }
            }
            "Few Clouds", "Scattered Clouds", "Broken Clouds", "Overcast Clouds",
            "Clouds", "Overcast", "Partly Clouds" -> {
                if (isDayTime) {
                    binding.root.setBackgroundResource(R.drawable.cloudy_clear)
                } else {
                    binding.root.setBackgroundResource(R.drawable.cloudy_night)
                }
            }
            "Mist", "Smoke", "Haze", "Fog", "Foggy", "Sand", "Dust", "Ash", "Squall" -> {
                if (isDayTime) {
                    binding.root.setBackgroundResource(R.drawable.foggy_way)
                } else {
                    binding.root.setBackgroundResource(R.drawable.hazzy_night)
                }
            }
            "Light Rain", "Moderate Rain", "Heavy Rain", "Very Heavy Rain", "Extreme Rain",
            "Freezing Rain", "Light Intensity Shower Rain", "Shower Rain", "Heavy Intensity Shower Rain",
            "Ragged Shower Rain", "Drizzle", "Light Drizzle", "Heavy Drizzle", "Showers" -> {
                binding.root.setBackgroundResource(R.drawable.rainyy)
            }
            "Snow", "Light Snow", "Heavy Snow", "Sleet", "Light Shower Snow", "Shower Snow",
            "Heavy Shower Snow", "Moderate Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_black)
            }
            "Thunderstorm", "Light Thunderstorm", "Heavy Thunderstorm", "Ragged Thunderstorm",
            "Thunderstorm with Light Rain", "Thunderstorm with Rain", "Thunderstorm with Heavy Rain",
            "Thunderstorm with Drizzle" -> {
                binding.root.setBackgroundResource(R.drawable.thunder_rain)
            }
            "Tornado" -> {
                binding.root.setBackgroundResource(R.drawable.tornado)
            }
            "Hurricane" -> {
                binding.root.setBackgroundResource(R.drawable.windy)
            }
            "Cold" -> {
                binding.root.setBackgroundResource(R.drawable.extreme_cold)
            }
            "Hot" -> {
                binding.root.setBackgroundResource(R.drawable.extreme_hot)
            }
            "Windy" -> {
                binding.root.setBackgroundResource(R.drawable.windy_mild)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_sea)
            }
        }
        val textViews = listOf(
            binding.forecastTitle,
            binding.tipsTitle

        )
        if (!isDayTime) {
            val whiteColor = ContextCompat.getColor(this, R.color.white)
            textViews.forEach { it.setTextColor(whiteColor) }
        }
    }



    private fun formatDay(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun getWeatherIcon(condition: String): Int {
        return when (condition.toLowerCase(Locale.ROOT)) {
            "clear,sunny" -> R.drawable.sunny
            "clouds,haze" -> R.drawable.cloud
            "rain,thunder" -> R.drawable.rain
            "snow" -> R.drawable.snow
            else -> R.drawable.sunny
        }
    }

}
data class WeatherTip(val iconResId: Int, val message: String)
