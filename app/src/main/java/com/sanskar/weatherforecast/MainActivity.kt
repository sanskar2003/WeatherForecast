package com.sanskar.weatherforecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sanskar.weather.models.WeatherResponse
import com.sanskar.weather.network.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var current_location: FusedLocationProviderClient
    private val permissionId = 2
    private lateinit var bottom_nav: BottomNavigationView
    private lateinit var location_text: TextView
    private var checked: String = "1"
    private lateinit var curr_loc: String
    private var latlng: LatLng? = null
    private var unitSystem: String = "Metric"

    private lateinit var wind_output: TextView
    private lateinit var temperature: TextView
    private lateinit var atm_status: TextView
    private lateinit var pressure_output: TextView
    private var rain_output: TextView? = null
    private lateinit var cloud_output: TextView
    private lateinit var visibility_output: TextView
    private lateinit var humidity_output: TextView
    private lateinit var temp_unit: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wind_output = findViewById(R.id.wind_output)
        atm_status = findViewById(R.id.atm_status)
        temperature = findViewById(R.id.temperature)
        pressure_output = findViewById(R.id.pressure_output)
        rain_output = findViewById(R.id.rain_output)
        cloud_output = findViewById(R.id.clouds_output)
        visibility_output = findViewById(R.id.visibility_output)
        humidity_output = findViewById(R.id.humidity_output)
        temp_unit = findViewById(R.id.c)

        location_text = findViewById(R.id.location)
        curr_loc = intent.getStringExtra("curr_loc").toString()
        checked = intent.getStringExtra("checked").toString()
        unitSystem = intent.getStringExtra("unitSystem").toString()
        current_location = LocationServices.getFusedLocationProviderClient(this)

        if (checked == "0") {
            location_text.text = curr_loc
            latlng = getCityLatitude(this, curr_loc as String?)
            if(latlng!=null) {
                Constants.curr_lat = latlng?.latitude!!
                Constants.curr_long = latlng?.longitude!!
                getWeatherDetails()
            }
        } else{
            getLocation()
        }

        bottom_nav = findViewById(R.id.bottom_nav)
        bottom_nav.selectedItemId = R.id.home
        bottom_nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    true
                }
                R.id.settings -> {
                    val intent = Intent(this@MainActivity, settings::class.java)
                    intent.putExtra("checked_home", checked)
                    intent.putExtra("curr_location", location_text.text.toString())
                    intent.putExtra("unitSystem", unitSystem)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> {
                    true
                }
            }
        }
    }
    private fun getWeatherDetails(){
        if(Constants.isNetworkAvailable(this)){
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service: WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<WeatherResponse> = service.getWeather(
                Constants.curr_lat, Constants.curr_long, Constants.APP_ID)

            listCall.enqueue(object: Callback<WeatherResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if(response.isSuccessful){
                        val weatherList: WeatherResponse = response.body()!!
                        updateUi(weatherList)
                    }
                    else{
                        getLocation()
                        Toast.makeText(this@MainActivity, "Invalid Data", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Error!!!", t!!.message.toString())
                }

            })
        }
        else{
            Toast.makeText(this, "No internet connection available.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(weatherList: WeatherResponse) {
        Log.i("Response Result", "$weatherList")
        if(unitSystem=="Imperial"){
            wind_output.text = ((weatherList.wind.speed * 2.2369362912 * 100.0).roundToInt()/100.0).toString() + " mi/h"
            temperature.text = ((((weatherList.main.temp - 273.15)*9/5 + 32)*10.0).roundToInt()/10.0).toString()
            temp_unit.text = "F"
            atm_status.text = weatherList.weather[0].main
            pressure_output.text = weatherList.main.pressure.roundToInt().toString() + " hPa"
            humidity_output.text = weatherList.main.humidity.toString() + "%"
            visibility_output.text = (weatherList.visibility / 1000).toString() + " km"
            cloud_output.text = weatherList.clouds.all.toString() + "%"
            rain_output?.text = weatherList.rain.toString() + ""
        }
        else {
            wind_output.text = ((weatherList.wind.speed * 3.6 * 100.0).roundToInt() / 100.0).toString() + " km/h"
            temperature.text = (((weatherList.main.temp - 273.15) * 10.0).roundToInt() / 10.0).toString()
            temp_unit.text = "C"
            atm_status.text = weatherList.weather[0].main
            pressure_output.text = weatherList.main.pressure.roundToInt().toString() + " hPa"
            humidity_output.text = weatherList.main.humidity.toString() + "%"
            visibility_output.text = (weatherList.visibility / 1000).toString() + " km"
            cloud_output.text = weatherList.clouds.all.toString() + "%"
            rain_output?.text = weatherList.rain.toString() + ""
        }
    }


    private fun getCityLatitude(context: MainActivity, city: String?): LatLng? {
        val geocoder = Geocoder(context, context.resources.configuration.locale)
        var addresses: List<Address>? = null
        var latLng: LatLng? = null
        try {
            addresses = geocoder.getFromLocationName(city!!, 1)
            val address = addresses!![0]
            latLng = LatLng(address.latitude, address.longitude)
        } catch (e: Exception) {
            getLocation()
            Toast.makeText(context, "Invalid Data", Toast.LENGTH_SHORT).show()
        }
        return latLng
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }


    private fun checkPermissions(): Boolean {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) &&
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED)){
            return true
        }
        return false
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                current_location.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) as List<Address>
                        list.apply {
                            location_text.text = list[0].locality
                            latlng = getCityLatitude(this@MainActivity, list[0].locality)
                            Constants.curr_lat = latlng?.latitude!!
                            Constants.curr_long = latlng?.longitude!!
                            getWeatherDetails()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
}