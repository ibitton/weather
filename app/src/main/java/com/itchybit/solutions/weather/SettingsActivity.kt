package com.itchybit.solutions.weather

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
	class SettingsFragment : PreferenceFragmentCompat() {
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		if(savedInstanceState == null) {
			supportFragmentManager
					.beginTransaction()
					.replace(R.id.settings, SettingsFragment())
					.commit()
		}
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this)
	}

	override fun onDestroy() {
		super.onDestroy()
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(this)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
		android.R.id.home -> {
			super.onBackPressed()
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		val darkModeString = getString(R.string.dark_mode_key)
		key?.let {
			if(it == darkModeString) sharedPreferences?.let { pref ->
				val darkModeValues = resources.getStringArray(R.array.dark_mode_values)
				when(pref.getString(darkModeString, darkModeValues[0])) {
					darkModeValues[0] -> AppCompatDelegate.setDefaultNightMode(
							AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
					darkModeValues[1] -> AppCompatDelegate.setDefaultNightMode(
							AppCompatDelegate.MODE_NIGHT_NO)
					darkModeValues[2] -> AppCompatDelegate.setDefaultNightMode(
							AppCompatDelegate.MODE_NIGHT_YES)
					darkModeValues[3] -> AppCompatDelegate.setDefaultNightMode(
							AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
				}
			}
		}
	}
}