package com.kgstorm.healthconnectbridge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kgstorm.healthconnectbridge.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main activity for the Health Connect Bridge app
 * Handles permission requests, settings configuration, and sync operations
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        healthConnectManager = HealthConnectManager(this)
        preferencesManager = PreferencesManager(this)

        // Set up permission launcher
        requestPermissionLauncher = registerForActivityResult(
            healthConnectManager.createPermissionRequestContract()
        ) { granted ->
            if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
                updatePermissionStatus(true)
                Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
            } else {
                updatePermissionStatus(false)
                showPermissionDeniedDialog()
            }
        }

        // Set up UI
        setupUI()
        
        // Check initial state
        checkHealthConnectAvailability()
        loadSettings()
        checkPermissions()
        updateLastSyncTime()
    }

    private fun setupUI() {
        // Permission button
        binding.requestPermissionsButton.setOnClickListener {
            requestHealthConnectPermissions()
        }

        // Save settings button
        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }

        // Sync now button
        binding.syncNowButton.setOnClickListener {
            performSync()
        }
    }

    private fun checkHealthConnectAvailability() {
        lifecycleScope.launch {
            val isAvailable = healthConnectManager.isAvailable()
            if (!isAvailable) {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Health Connect Not Available")
                    .setMessage("Health Connect is not available on this device. Please install it from the Play Store.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    private fun checkPermissions() {
        lifecycleScope.launch {
            val hasPermissions = healthConnectManager.hasAllPermissions()
            updatePermissionStatus(hasPermissions)
        }
    }

    private fun updatePermissionStatus(granted: Boolean) {
        binding.permissionStatusText.text = if (granted) {
            getString(R.string.permissions_granted)
        } else {
            getString(R.string.permissions_denied)
        }
        
        binding.requestPermissionsButton.isEnabled = !granted
    }

    private fun requestHealthConnectPermissions() {
        requestPermissionLauncher.launch(HealthConnectManager.PERMISSIONS)
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required")
            .setMessage("This app requires Health Connect permissions to function. Please grant the permissions.")
            .setPositiveButton("Try Again") { _, _ ->
                requestHealthConnectPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val url = preferencesManager.homeAssistantUrl.first()
            val token = preferencesManager.homeAssistantToken.first()
            
            binding.haUrlInput.setText(url ?: "")
            binding.haTokenInput.setText(token ?: "")
        }
    }

    private fun saveSettings() {
        val url = binding.haUrlInput.text?.toString()?.trim() ?: ""
        val token = binding.haTokenInput.text?.toString()?.trim() ?: ""

        if (url.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Basic URL validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "URL must start with http:// or https://", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                preferencesManager.saveHomeAssistantUrl(url)
                preferencesManager.saveHomeAssistantToken(token)
                Toast.makeText(
                    this@MainActivity,
                    R.string.settings_saved,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.settings_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun performSync() {
        lifecycleScope.launch {
            // Check if settings are configured
            val url = preferencesManager.homeAssistantUrl.first()
            val token = preferencesManager.homeAssistantToken.first()

            if (url.isNullOrBlank() || token.isNullOrBlank()) {
                Toast.makeText(
                    this@MainActivity,
                    "Please configure Home Assistant settings first",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Check permissions
            val hasPermissions = healthConnectManager.hasAllPermissions()
            if (!hasPermissions) {
                Toast.makeText(
                    this@MainActivity,
                    "Please grant Health Connect permissions first",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // TODO: Implement actual sync logic
            // For now, just update the last sync time
            Toast.makeText(
                this@MainActivity,
                "Sync functionality will be implemented in the next phase",
                Toast.LENGTH_LONG
            ).show()

            preferencesManager.saveLastSyncTime(System.currentTimeMillis())
            updateLastSyncTime()
        }
    }

    private fun updateLastSyncTime() {
        lifecycleScope.launch {
            val lastSync = preferencesManager.lastSyncTime.first()
            binding.lastSyncText.text = if (lastSync != null) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                getString(R.string.last_sync, dateFormat.format(Date(lastSync)))
            } else {
                getString(R.string.never_synced)
            }
        }
    }
}
