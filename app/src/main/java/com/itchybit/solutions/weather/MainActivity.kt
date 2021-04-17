package com.itchybit.solutions.weather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.itchybit.solutions.weather.Utils.getLongTimeString
import com.itchybit.solutions.weather.Utils.getTempString
import com.itchybit.solutions.weather.Utils.getTimeString
import com.itchybit.solutions.weather.Utils.getTimezoneString
import com.itchybit.solutions.weather.databinding.ActivityMainBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
	companion object {
		val TAG: String = MainActivity::class.java.name
	}

	private lateinit var binding: ActivityMainBinding
	private val model: ReportViewModel by viewModels()
	private val launcher = registerForActivityResult(RequestPermission()) {
		if(it) {
			getLocation()
		} else {
			binding.swipe.isRefreshing = false
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.swipe.setOnRefreshListener(::getLocation)
		model.getLocation().observe(this, { location ->
			if(model.getReport().value == null || binding.swipe.isRefreshing) {
				getReport(location!!)
			}
		})
		model.getReport().observe(this, {
			updateUI(it!!)
		})
		if(model.getLocation().value == null) {
			getLocation()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
		R.id.refresh -> {
			getLocation()
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	private fun getLocation() {
		binding.swipe.isRefreshing = true
		if(ActivityCompat.checkSelfPermission(this,
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
		} else {
			LocationServices.getFusedLocationProviderClient(this).lastLocation
					.addOnSuccessListener(::onLastLocation)
		}
	}

	private fun onLastLocation(location: Location?) {
		if(location != null) {
			model.setLocation(location)
		} else {
			binding.swipe.isRefreshing = false
			Toast.makeText(this, getString(R.string.no_location_error), Toast.LENGTH_LONG).show()
		}
	}

	private fun getReport(location: Location) {
		Volley.newRequestQueue(this).add(StringRequest(Request.Method.GET,
				getString(R.string.weather_current_geo, location.latitude, location.longitude), {
			Log.d(TAG, it)
			model.setReport(Gson().fromJson(it, Report::class.java))
		}, {
			Log.d(TAG, it.message!!)
			Toast.makeText(this@MainActivity, getString(R.string.report_error, it.message),
					Toast.LENGTH_LONG).show()
			binding.swipe.isRefreshing = false
		}))
	}

	private fun updateUI(report: Report) {
		binding.city.text = report.name
		binding.cloudiness.text = getString(R.string.percentage, report.clouds.all)
		binding.country.text = report.sys.country
		binding.description.text = report.weather[0].description
		binding.dt.text = getLongTimeString(report.dt)
		binding.feelsLike.text = getTempString(report.main.feelsLike)
		binding.lat.text = getString(R.string.degrees_float, report.coord.lat)
		binding.lon.text = getString(R.string.degrees_float, report.coord.lon)
		binding.pressure.text = getString(R.string.pressure_format, report.main.pressure)
		binding.humidity.text = getString(R.string.percentage, report.main.humidity)
		binding.sunriseTime.text = getTimeString(report.sys.sunrise)
		binding.sunsetTime.text = getTimeString(report.sys.sunset)
		if(report.main.seaLevel == null) {
			binding.seaLevelPressureLabel.visibility = View.GONE
			binding.seaLevelPressure.visibility = View.GONE
		} else {
			binding.seaLevelPressure.text = report.main.seaLevel.toString()
		}
		if(report.main.groundLevel == null) {
			binding.groundLevelPressureLabel.visibility = View.GONE
			binding.groundLevelPressure.visibility = View.GONE
		} else {
			binding.groundLevelPressure.text = report.main.groundLevel.toString()
		}
		binding.temp.text = getTempString(report.main.temp)
		binding.tempMin.text = getTempString(report.main.tempMin)
		binding.tempMax.text = getTempString(report.main.tempMax)
		binding.timezone.text = getTimezoneString(report.timezone)
		binding.visibility.text = getString(R.string.meters, report.visibility)
		binding.windSpeed.text = getString(R.string.meters_per_second, report.wind.speed)
		binding.windDegrees.text = getString(R.string.degrees_int, report.wind.deg)
		if(report.wind.gust == null) {
			binding.windGustLabel.visibility = View.GONE
			binding.windGust.visibility = View.GONE
		} else {
			binding.windGust.text = getString(R.string.meters_per_second, report.wind.gust)
		}
		binding.weather.text = report.weather[0].main
		if(report.rain == null) {
			binding.rainHeader.visibility = View.GONE
			binding.rainLastHourLabel.visibility = View.GONE
			binding.rainLastHour.visibility = View.GONE
			binding.rainLast3HoursLabel.visibility = View.GONE
			binding.rainLast3Hours.visibility = View.GONE
		} else {
			binding.rainLastHour.text = getString(R.string.millimeters, report.rain.oneHour)
			binding.rainLast3Hours.text = getString(R.string.millimeters, report.rain.threeHours)
		}
		if(report.snow == null) {
			binding.snowHeader.visibility = View.GONE
			binding.snowLastHourLabel.visibility = View.GONE
			binding.snowLastHour.visibility = View.GONE
			binding.snowLast3HoursLabel.visibility = View.GONE
			binding.snowLast3Hours.visibility = View.GONE
		} else {
			binding.snowLastHour.text = getString(R.string.millimeters, report.snow.oneHour)
			binding.snowLast3Hours.text = getString(R.string.millimeters, report.snow.threeHours)
		}
		binding.swipe.isRefreshing = false
		Picasso.get().load(getString(R.string.weather_icon, report.weather[0].icon,
				getString(R.string.icon_4x)))
				.into(binding.icon, object : Callback {
					override fun onSuccess() {}
					override fun onError(e: Exception?) {
						Log.d(TAG, e?.message!!)
					}
				})
	}
}