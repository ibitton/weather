package com.itchybit.solutions.weather

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReportViewModel : ViewModel() {
	private val location: MutableLiveData<Location?> = MutableLiveData()
	private val report: MutableLiveData<Report?> = MutableLiveData()

	fun getLocation(): LiveData<Location?> = location
	fun setLocation(newLocation: Location?) {
		location.value = newLocation
	}

	fun getReport(): LiveData<Report?> = report
	fun setReport(newReport: Report?) {
		report.value = newReport
	}
}