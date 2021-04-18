package com.itchybit.solutions.weather

import com.google.gson.annotations.SerializedName

/**
 * A DTO for weather report from OpenWeather (https://openweathermap.org/current#current_JSON).
 */

data class Coord(val lat: Double?, val lon: Double?)
data class Weather(val id: Int?, val main: String?, val description: String?, val icon: String?)
data class Main(
		val temp: Double?, @SerializedName("feels_like") val feelsLike: Double?, val pressure: Int?,
		val humidity: Int?, @SerializedName("temp_min") val tempMin: Double?,
		@SerializedName("temp_max") val tempMax: Double?,
		@SerializedName("sea_level") val seaLevel: Int?,
		@SerializedName("ground_level") val groundLevel: Int?,
)

data class Wind(val speed: Double?, val deg: Int?, val gust: Double?)
data class Clouds(val all: Int?)
data class Rain(@SerializedName("1h") val oneHour: Int?, @SerializedName("3h") val threeHours: Int?)
data class Snow(@SerializedName("1h") val oneHour: Int?, @SerializedName("3h") val threeHours: Int?)
data class Sys(
		val type: Int?, val id: Int?, val message: Double?, val country: String?,
		val sunrise: Long?,
		val sunset: Long?,
)

data class Report(
		val coord: Coord?, val weather: List<Weather>?, val base: String?, val main: Main?,
		val visibility: Int?, val wind: Wind?, val clouds: Clouds?, val rain: Rain?,
		val snow: Snow?,
		val dt: Long?, val sys: Sys?, val timezone: Int?, val id: Long?, val name: String?,
		val cod: Int?,
)
