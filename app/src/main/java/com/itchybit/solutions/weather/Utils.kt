package com.itchybit.solutions.weather

import android.os.Build
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.roundToInt

object Utils {
	fun getTimezoneString(timezone: Int) =
			(timezone / 3600).let { "UTC${if(it >= 0) "+" else "-"}$it" }

	fun getLongTimeString(time: Long): String =
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
						.toString()
			} else {
				val sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL)
				val date = Date(time * 1000)
				sdf.format(date)
			}

	fun getTimeString(time: Long): String =
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
						.toLocalTime().toString()
			} else {
				val sdf = SimpleDateFormat.getTimeInstance()
				val date = Date(time * 1000)
				sdf.format(date)
			}

	fun getTempString(temp: Double) =
			(((temp - 273.15) * 100).roundToInt() / 100).toString() + "\u2103"
}