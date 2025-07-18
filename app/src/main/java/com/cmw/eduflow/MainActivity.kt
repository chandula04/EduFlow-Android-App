package com.cmw.eduflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cmw.eduflow.databinding.ActivityMainBinding
import com.cloudinary.android.MediaManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration for Cloudinary (if you are using it)
        // Make sure to replace with your actual credentials
        val config = mapOf(
            "cloud_name" to "depub6uly",
            "api_key" to "589664948319453",
            "api_secret" to "mtfu6p8vr2uFY1mkaaXN7Oe_gxY"
        )
        MediaManager.init(this, config)
    }
}