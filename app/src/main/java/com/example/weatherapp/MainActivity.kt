package com.example.weatherapp

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var city: String = "Barcelona,ES"
    val api: String = "0a2e557f41a5a091276f0f53b5de62c3"

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        screenSplash.setKeepOnScreenCondition{false}
        
        binding.buttonSearch.setOnClickListener {
            if(binding.cityTextView.text.isNotEmpty()){
                city = binding.cityTextView.text.toString()
                weatherTask().execute()
            } else {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            }
        }
        
        weatherTask().execute()
    }

    inner class weatherTask() : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            binding.loader.visibility = View.VISIBLE
            binding.mainContainer.visibility = View.GONE
            binding.errortext.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$api&units=metric").readText(Charsets.UTF_8)
            }
            catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                binding.address.text = address
                binding.updatedAt.text = updatedAtText
                binding.status.text = weatherDescription.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.ROOT
                    ) else it.toString()
                }
                binding.temp.text = temp
                binding.tempMin.text = tempMin
                binding.tempMax.text = tempMax
                binding.sunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                binding.sunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                binding.wind.text = windSpeed
                binding.pressure.text = pressure
                binding.humidity.text = humidity

                binding.loader.visibility = View.GONE
                binding.mainContainer.visibility = View.VISIBLE
            }
            catch (e: java.lang.Exception){
                Toast.makeText(this@MainActivity, "City not found, try another one", Toast.LENGTH_SHORT).show()
                recreate()
                binding.cityTextView.text.clear()
                //binding.loader.visibility = View.GONE
                //binding.errortext.visibility = View.VISIBLE
            }
        }
    }
}