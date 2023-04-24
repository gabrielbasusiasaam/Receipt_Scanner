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

        // Detects the Theme option being changed. This does not have to handle storage of the new
        // value as that is done automatically when the value changes
        darkModePreference?.setOnPreferenceChangeListener { _, newValue ->
            // Sets the Apps theme to the selected value
            val mode = if (newValue as Boolean)
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)

            // Refreshes the screen once the user changes their theme to apply the changes visually
            activity.invalidateMenu()
            true
        }
    }
}