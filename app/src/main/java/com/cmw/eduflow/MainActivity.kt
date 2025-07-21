package com.cmw.eduflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cmw.eduflow.databinding.ActivityMainBinding
import com.cloudinary.android.MediaManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Handles the result of the permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration for Cloudinary with your credentials
        val config = mapOf(
            "cloud_name" to "depub6uly",
            "api_key" to "589664948319453",
            "api_secret" to "mtfu6p8vr2uFY1mkaaXN7Oe_gxY"
        )
        MediaManager.init(this, config)

        // Ask for notification permission when the app starts
        askNotificationPermission()
    }

    // This function checks and asks for notification permission
    private fun askNotificationPermission() {
        // This is required for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}