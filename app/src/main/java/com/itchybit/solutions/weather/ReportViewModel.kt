package com.itchybit.solutions.weather

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReportViewModel : ViewModel() {
	val location: MutableLiveData<Location?> = MutableLiveData()
	val report: MutableLiveData<Report?> = MutableLiveData()
}