package com.example.weatherapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupHourlyRecyclerView()
        fetchWeatherData("lahore")
        searchCity()
        binding.button.setOnClickListener {
            val intent = Intent(this, ObserveActivity::class.java).apply {
                putExtra("cityName", binding.cityName.text.toString().replace("City: ", ""))
            }
            startActivity(intent)
        }

    }
    private fun setupHourlyRecyclerView() {
        val hourlyAdapter = HourlyWeatherAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
    }
    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
    private fun fetchWeatherData(cityName:String) {
       val retrofit=Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(APIInterface::class.java)
        val currentWeatherCall = retrofit.getWeatherDta(cityName, "fbeb38e5a71bb9df72575e80c11991c8", "metric")
        val hourlyForecastCall = retrofit.getHourlyForecast(cityName, "fbeb38e5a71bb9df72575e80c11991c8", "metric")
        val response=retrofit.getWeatherDta(cityName,"fbeb38e5a71bb9df72575e80c11991c8","metric")
        response.enqueue(object:Callback<weatherApp>{
            override fun onResponse(call: Call<weatherApp>, response: Response<weatherApp>) {
                val responseBody=response.body()
                if(response.isSuccessful && responseBody!=null)
                {
                    val temperature=responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong() * 1000
                    val sunSet = responseBody.sys.sunset.toLong() * 1000
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    val currentTime = System.currentTimeMillis()
                    val isDayTime = currentTime in sunRise..sunSet
                    val lat = responseBody.coord.lat
                    val lon = responseBody.coord.lon
                    val airQualityCall = retrofit.getAirQuality(lat, lon, "fbeb38e5a71bb9df72575e80c11991c8")
                    airQualityCall.enqueue(object : Callback<AirQualityResponse> {
                        override fun onResponse(call: Call<AirQualityResponse>, response: Response<AirQualityResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                val aqi = response.body()!!.list.first().main.aqi
                                val message = getAQIMessage(aqi)
                                binding.message.text = " Air Quality: $aqi - $message  "
                            }
                        }
                        override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                            Log.e(TAG, "Air quality API failed: ${t.message}")
                        }
                    })
                    binding.temp.text="$temperature °C"
                    binding.weather.text=condition
                    binding.humidity.text = "$humidity%"
                    binding.windspeed.text = "$windSpeed m/s"
                    binding.sunrise.text = "$sunRise"
                    binding.sunset.text = "$sunSet"
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition
                    binding.maxTemp.text = "Max: $maxTemp °C"
                    binding.minTemp.text = "Min: $minTemp °C"
                    binding.day.text=dayName(System.currentTimeMillis())
                    binding.date.text=date()
                    binding.time.text=time()
                    binding.cityName.text="$cityName"

                    changeImagesAccordingToWeatherConditions(condition,isDayTime)
                }
            }
            override fun onFailure(call: Call<weatherApp>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}")
            }
        })
        hourlyForecastCall.enqueue(object : Callback<HourlyForecastResponse> {
            override fun onResponse(call: Call<HourlyForecastResponse>, response: Response<HourlyForecastResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val hourlyData = response.body()!!.list.map { forecastItem ->
                        HourlyWeather(
                            time = formatHour(forecastItem.dt),
                            temperature = "${forecastItem.main.temp.toInt()}°C",
                            icon = getWeatherIcon(forecastItem.weather.firstOrNull()?.main ?: "unknown")
                        )
                    }
                    val hourlyAdapter = HourlyWeatherAdapter(hourlyData)
                    binding.recyclerView.adapter = hourlyAdapter
                }
            }
            override fun onFailure(call: Call<HourlyForecastResponse>, t: Throwable) {
                Log.e(TAG, "Hourly forecast API call failed: ${t.message}")
            }
        })

    }
    private fun getAQIMessage(aqi: Int): String {
        return when (aqi) {
            1 -> "Good — Air quality is considered satisfactory."
            2 -> "Fair — Air quality is acceptable."
            3 -> "Moderate — Sensitive groups should reduce outdoor activity."
            4 -> "Poor — Unhealthy for sensitive groups."
            5 -> "Very Poor — Everyone may experience more serious effects."
            else -> "Unknown"
        }
    }
    private fun formatHour(timestamp: Long): String {
        val sdf = SimpleDateFormat("h a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
    private fun getWeatherIcon(condition: String): Int {
        return when (condition) {
            "Clear" -> R.drawable.sunny
            "Clouds" -> R.drawable.white_cloud
            "Rain" -> R.drawable.rain
            "Snow" -> R.drawable.snow
            else -> R.drawable.sunny
        }
    }
    private fun changeImagesAccordingToWeatherConditions(conditions: String,isDayTime: Boolean) {
        when (conditions) {
            "Clear", "Sunny" -> {
                if (isDayTime) {
                    binding.root.setBackgroundResource(R.drawable.sunny_mountain)
                    binding.lottieAnimationView.setAnimation(R.raw.sun)
                } else {
                    binding.root.setBackgroundResource(R.drawable.clear_night)
                    binding.lottieAnimationView.setAnimation(R.raw.night_moonn)
                }
            }
            "Few Clouds", "Scattered Clouds", "Broken Clouds", "Overcast Clouds",
            "Clouds", "Overcast", "Partly Clouds" -> {
                if (isDayTime) {
                    binding.root.setBackgroundResource(R.drawable.cloudy_clear)
                    binding.lottieAnimationView.setAnimation(R.raw.cloud)
                } else {
                    binding.root.setBackgroundResource(R.drawable.cloudy_night)
                    binding.lottieAnimationView.setAnimation(R.raw.cloud)
                }
            }
            "Mist", "Smoke", "Haze", "Fog", "Foggy", "Sand", "Dust", "Ash", "Squall" -> {
                if (isDayTime) {
                    binding.root.setBackgroundResource(R.drawable.foggy_way)
                    binding.lottieAnimationView.setAnimation(R.raw.cloud)
                } else {
                    binding.root.setBackgroundResource(R.drawable.hazzy_night)
                    binding.lottieAnimationView.setAnimation(R.raw.cloud)
                }
            }
            "Light Rain", "Moderate Rain", "Heavy Rain", "Very Heavy Rain", "Extreme Rain",
            "Freezing Rain", "Light Intensity Shower Rain", "Shower Rain", "Heavy Intensity Shower Rain",
            "Ragged Shower Rain", "Drizzle", "Light Drizzle", "Heavy Drizzle", "Showers" -> {
                binding.root.setBackgroundResource(R.drawable.rainyy)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Snow", "Light Snow", "Heavy Snow", "Sleet", "Light Shower Snow", "Shower Snow",
            "Heavy Shower Snow", "Moderate Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_black)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            "Thunderstorm", "Light Thunderstorm", "Heavy Thunderstorm", "Ragged Thunderstorm",
            "Thunderstorm with Light Rain", "Thunderstorm with Rain", "Thunderstorm with Heavy Rain",
            "Thunderstorm with Drizzle" -> {
                binding.root.setBackgroundResource(R.drawable.thunder_rain)
                binding.lottieAnimationView.setAnimation(R.raw.thunderrain)
            }
            "Tornado" -> {
                binding.root.setBackgroundResource(R.drawable.tornado)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Hurricane" -> {
                binding.root.setBackgroundResource(R.drawable.windy)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
             "Cold" -> {
                binding.root.setBackgroundResource(R.drawable.extreme_cold)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            "Hot" -> {
                binding.root.setBackgroundResource(R.drawable.extreme_hot)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Windy" -> {
                binding.root.setBackgroundResource(R.drawable.windy_mild)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_sea)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        val textViews = listOf(
            binding.textView11,
            binding.temp,
            binding.maxTemp,
            binding.minTemp,
            binding.cityName,
            binding.day,
            binding.date,
            binding.weather,
            binding.textView,
            binding.time,
            binding.message
        )

        if (!isDayTime) {
            val whiteColor = ContextCompat.getColor(this, R.color.white)
            textViews.forEach { it.setTextColor(whiteColor) }
        }

        binding.lottieAnimationView.playAnimation()
    }

}

fun dayName(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
    return sdf.format(Date())
}
private fun date(): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(Date())
}

private fun time(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

