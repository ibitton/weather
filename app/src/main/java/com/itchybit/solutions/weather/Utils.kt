package com.itchybit.solutions.weather

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

object Utils {
	enum class TempUnit(val value: String) {
		KELVIN("kelvin"), CELSIUS("celsius"), FAHRENHEIT("fahrenheit");

		companion object {
			fun getByValue(value: String) = values().firstOrNull { it.value == value }
		}
	}

	enum class SpeedUnit(val value: String) {
		METERS_PER_SECOND("meters_per_second"), MILES_PER_HOUR("miles_per_hour");

		companion object {
			fun getByValue(value: String) = values().firstOrNull { it.value == value }
		}
	}

	enum class TimeFormat(val value: Int) {
		HOURS_12(12), HOURS_24(24);

		companion object {
			fun getByValue(value: Int) = values().firstOrNull { it.value == value }
		}
	}

	enum class IconSize(val value: Int) {
		X1(1), X2(2), X4(4);

		companion object {
			fun getByValue(value: Int) = values().firstOrNull { it.value == value }
		}
	}

	fun getTimezoneString(timezone: Int?, context: Context): String = when(timezone) {
		null -> ""
		else -> {
			(timezone / 3600).let {
				context.getString(R.string.timezone, if(it >= 0) "+" else "-", it.toString())
			}
		}
	}

	fun getDateTimeString(time: Long?, timeFormat: TimeFormat): String =
			when {
				time == null -> ""
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
					ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
							.let {
								when(timeFormat) {
									TimeFormat.HOURS_24 ->
										it.toString()
									TimeFormat.HOURS_12 -> it.format(
											DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a z"))
								}
							}
				}
				else -> {
					val sdf = when(timeFormat) {
						TimeFormat.HOURS_24 -> SimpleDateFormat.getDateTimeInstance(
								DateFormat.SHORT,
								DateFormat.FULL)
						TimeFormat.HOURS_12 -> SimpleDateFormat("yyyy/MM/dd hh:mm:ss a z",
								Locale.US)
					}
					val date = Date(time * 1000)
					sdf.format(date)
				}
			}

	fun getTimeString(time: Long?, timeFormat: TimeFormat): String =
			when {
				time == null -> ""
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
					ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
							.toLocalTime().let {
								when(timeFormat) {
									TimeFormat.HOURS_24 -> it.toString()
									TimeFormat.HOURS_12 -> it.format(
											DateTimeFormatter.ofPattern("hh:mm:ss a"))
								}
							}
				}
				else -> {
					val sdf = when(timeFormat) {
						TimeFormat.HOURS_24 -> SimpleDateFormat.getTimeInstance()
						TimeFormat.HOURS_12 -> SimpleDateFormat("hh:mm:ss a", Locale.US)
					}
					val date = Date(time * 1000)
					sdf.format(date)
				}
			}

	fun getTempString(temp: Double?, tempUnit: TempUnit, context: Context) =
			when(temp) {
				null -> ""
				else -> when(tempUnit) {
					TempUnit.KELVIN -> context.getString(R.string.kelvin, temp.roundToInt())
					TempUnit.CELSIUS -> context.getString(R.string.celsius,
							getCelsiusFromKelvin(temp).roundToInt())
					TempUnit.FAHRENHEIT -> context.getString(R.string.fahrenheit,
							getFahrenheitFromKelvin(temp).roundToInt())
				}
			}

	fun getSpeedString(speed: Double?, speedUnit: SpeedUnit, context: Context) =
			when(speed) {
				null -> ""
				else -> when(speedUnit) {
					SpeedUnit.METERS_PER_SECOND -> context.getString(
							R.string.meters_per_second, speed)
					SpeedUnit.MILES_PER_HOUR -> context.getString(R.string.miles_per_hour,
							speed * 2.23694)
				}
			}

	fun getIconSize(iconSize: IconSize, context: Context) = when(iconSize) {
		IconSize.X1 -> context.getString(R.string.icon_1x)
		IconSize.X2 -> context.getString(R.string.icon_2x)
		IconSize.X4 -> context.getString(R.string.icon_4x)
	}

	fun getDarkMode(value: String, context: Context) = when(value) {
		context.getString(
				R.string.MODE_NIGHT_FOLLOW_SYSTEM),
		-> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		context.getString(R.string.MODE_NIGHT_NO) -> AppCompatDelegate.MODE_NIGHT_NO
		context.getString(R.string.MODE_NIGHT_YES) -> AppCompatDelegate.MODE_NIGHT_YES
		context.getString(
				R.string.MODE_NIGHT_AUTO_BATTERY),
		-> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
		else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
	}

	private fun getCelsiusFromKelvin(kelvin: Double) = kelvin - 273.15
	private fun getFahrenheitFromKelvin(kelvin: Double) =
			getCelsiusFromKelvin(kelvin) * 9 / 5 + 32
}