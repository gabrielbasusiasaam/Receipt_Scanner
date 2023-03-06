package com.app.receiptscanner.layouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.PreferenceManager
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkKey = getString(R.string.dark_mode)
        val mode = if(preferences.getBoolean(darkKey, false)) MODE_NIGHT_YES else MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
        setContentView(view)
    }

}