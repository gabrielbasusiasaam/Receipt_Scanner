package com.app.receiptscanner.layouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.app.receiptscanner.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val activity by lazy { requireActivity() as MainActivity }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val darkKey = getString(R.string.dark_mode)
        val darkModePreference: SwitchPreference? = findPreference(darkKey)

        darkModePreference?.setOnPreferenceChangeListener { _, newValue ->
            val mode = if (newValue as Boolean)
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            activity.invalidateMenu()
            true
        }
    }
}