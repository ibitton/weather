package com.itchybit.solutions.weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.itchybit.solutions.weather.Utils.getDarkMode
import com.itchybit.solutions.weather.Utils.getDateTimeString
import com.itchybit.solutions.weather.Utils.getIconSize
import com.itchybit.solutions.weather.Utils.getSpeedString
import com.itchybit.solutions.weather.Utils.getTempString
import com.itchybit.solutions.weather.Utils.getTimeString
import com.itchybit.solutions.weather.Utils.getTimezoneString
import com.itchybit.solutions.weather.databinding.ActivityMainBinding
import com.mikepenz.aboutlibraries.LibsBuilder
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
		AppCompatDelegate.setDefaultNightMode(getDarkMode(
				PreferenceManager.getDefaultSharedPreferences(this)
						.getString(getString(R.string.dark_mode_key),
								getString(R.string.default_dark_mode))!!, this))
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.swipe.setOnRefreshListener(::getLocation)
		model.location.observe(this, {
			if(model.report.value == null || binding.swipe.isRefreshing) {
				getReport(it!!)
			}
		})
		model.report.observe(this, {
			updateUI(it!!)
		})

		if(model.location.value == null) {
			getLocation()
		}
	}

	override fun onResume() {
		super.onResume()
		model.report.value.let {
			if(it != null) {
				updateUI(it)
			}
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
		R.id.settings -> {
			startActivity(Intent(this, SettingsActivity::class.java))
			true
		}
		R.id.about_libraries -> {
			LibsBuilder().start(this)
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
			model.location.value = location
		} else {
			binding.swipe.isRefreshing = false
			Toast.makeText(this, getString(R.string.no_location_error), Toast.LENGTH_LONG).show()
		}
	}

	private fun getReport(location: Location) {
		Volley.newRequestQueue(this).add(StringRequest(Request.Method.GET,
				getString(R.string.weather_current_geo, location.latitude, location.longitude), {
			Log.d(TAG, it)
			model.report.value = Gson().fromJson(it, Report::class.java)
		}, {
			Log.d(TAG, it.message!!)
			Toast.makeText(this@MainActivity, getString(R.string.report_error, it.message),
					Toast.LENGTH_LONG).show()
			binding.swipe.isRefreshing = false
		}))
	}

	private fun updateUI(report: Report) {
		PreferenceManager.getDefaultSharedPreferences(this).let {
			val tempUnit = Utils.TempUnit.getByValue(it.getString(getString(R.string.temp_unit_key),
					Utils.TempUnit.CELSIUS.value)!!)!!
			val speedUnit =
					Utils.SpeedUnit.getByValue(it.getString(getString(R.string.speed_unit_key),
							Utils.SpeedUnit.METERS_PER_SECOND.value)!!)!!
			val timeFormat =
					Utils.TimeFormat.getByValue(it.getString(getString(R.string.time_format_key),
							Utils.TimeFormat.HOURS_24.value.toString())!!.toInt())!!
			val iconSize = Utils.IconSize.getByValue(it.getString(getString(R.string.icon_size_key),
					Utils.IconSize.X4.value.toString())!!.toInt())!!
			binding.apply {
				updateField(dt, report.dt, value = getDateTimeString(report.dt, timeFormat))
				updateField(sunrise, report.sys?.sunrise, label = sunriseLabel,
						value = getTimeString(report.sys?.sunrise, timeFormat))
				updateField(sunset, report.sys?.sunset, label = sunsetLabel,
						value = getTimeString(report.sys?.sunset, timeFormat))
				updateField(timezone, report.timezone,
						value = getTimezoneString(report.timezone, this@MainActivity))
				updateField(lat, report.coord?.lat, label = latLabel,
						value = getString(R.string.degrees_float, report.coord?.lat))
				updateField(lon, report.coord?.lon, label = lonLabel,
						value = getString(R.string.degrees_float, report.coord?.lon))
				updateField(city, report.name)
				updateField(country, report.sys?.country)
				updateField(weather, report.weather?.get(0)?.main)
				updateField(description, report.weather?.get(0)?.description)
				updateField(temp, report.main?.temp, label = tempHeader,
						value = getTempString(report.main?.temp, tempUnit, this@MainActivity))
				updateField(tempMin, report.main?.tempMin, label = tempMinLabel,
						value = getTempString(report.main?.tempMin, tempUnit, this@MainActivity))
				updateField(tempMax, report.main?.tempMax, label = tempMaxLabel,
						value = getTempString(report.main?.tempMax, tempUnit, this@MainActivity))
				updateField(feelsLike, report.main?.feelsLike, label = feelsLikeLabel,
						value = getTempString(report.main?.feelsLike, tempUnit, this@MainActivity))
				updateField(windSpeed, report.wind?.speed, label = windHeader,
						value = getSpeedString(report.wind?.speed, speedUnit, this@MainActivity))
				updateField(windDegrees, report.wind?.deg, label = windDegreesLabel,
						value = getString(R.string.degrees_int, report.wind?.deg))
				updateField(windGust, report.wind?.gust, label = windGustLabel,
						value = getSpeedString(report.wind?.gust, speedUnit, this@MainActivity))
				updateField(pressure, report.main?.pressure, label = pressureHeader,
						value = getString(R.string.pressure_format, report.main?.pressure))
				updateField(seaLevelPressure, report.main?.seaLevel, label = seaLevelPressureLabel,
						value = getString(R.string.pressure_format, report.main?.seaLevel))
				updateField(groundLevelPressure, report.main?.groundLevel,
						label = groundLevelPressureLabel,
						value = getString(R.string.pressure_format, report.main?.groundLevel))
				updateField(humidity, report.main?.humidity, label = humidityHeader,
						value = getString(R.string.percentage, report.main?.humidity))
				updateField(visibility, report.visibility, label = visibilityHeader,
						value = getString(R.string.meters, report.visibility))
				updateField(cloudiness, report.clouds, label = cloudinessHeader,
						value = getString(R.string.percentage, report.clouds?.all))
				rainHeader.visibility = if(report.rain == null) View.GONE else View.VISIBLE
				updateField(rainLastHour, report.rain?.oneHour, label = rainLastHourLabel,
						value = getString(R.string.millimeters, report.rain?.oneHour))
				updateField(rainLast3Hours, report.rain?.threeHours, label = rainLast3HoursLabel,
						value = getString(R.string.millimeters, report.rain?.threeHours))
				snowHeader.visibility = if(report.snow == null) View.GONE else View.VISIBLE
				updateField(snowLastHour, report.snow?.oneHour, label = snowLastHourLabel,
						value = getString(R.string.millimeters, report.snow?.oneHour))
				updateField(snowLast3Hours, report.snow?.threeHours, label = snowLast3HoursLabel,
						value = getString(R.string.millimeters, report.snow?.threeHours))
				swipe.isRefreshing = false
				if(report.weather?.get(0) != null) {
					icon.visibility = View.VISIBLE
					Picasso.get()
							.load(getString(R.string.weather_icon, report.weather[0].icon,
									getIconSize(iconSize, this@MainActivity)))
							.into(icon, object : Callback {
								override fun onSuccess() {}
								override fun onError(e: Exception?) {
									Log.d(TAG, e?.message!!)
								}
							})
				} else {
					icon.visibility = View.GONE
				}
			}
		}
	}

	private fun updateField(
			field: TextView, reportValue: Any?, label: TextView? = null,
			value: String? = null,
	) {
		if(reportValue != null) {
			field.apply {
				visibility = View.VISIBLE
				text = value ?: reportValue.toString()
			}
			label?.apply { visibility = View.VISIBLE }
		} else {
			field.visibility = View.GONE
			label?.visibility = View.GONE
		}
	}
}