package com.example.app

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE)
                    as DevicePolicyManager

        compName = ComponentName(
            this,
            MyDeviceAdminReceiver::class.java
        )

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {

        // Accessibility
        if (!isAccessibilityEnabled()) {

            showPopup(
                "Accessibility Permission",
                "Please enable accessibility permission"
            ) {

                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }

            return
        }

        // Overlay Permission
        if (!Settings.canDrawOverlays(this)) {

            showPopup(
                "Display Over Apps",
                "Allow display over other apps"
            ) {

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                startActivity(intent)
            }

            return
        }

        // Media Permission
        if (!hasMediaPermission()) {

            showPopup(
                "Media Permission",
                "Allow media/photos access"
            ) {

                requestMediaPermission.launch(
                    getMediaPermissions()
                )
            }

            return
        }

        // Battery Optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val pm =
                getSystemService(Context.POWER_SERVICE)
                        as PowerManager

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {

                showPopup(
                    "Battery Optimization",
                    "Disable battery optimization"
                ) {

                    val intent = Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )

                    startActivity(intent)
                }

                return
            }
        }

        // Device Admin
        if (!devicePolicyManager.isAdminActive(compName)) {

            showPopup(
                "Device Admin",
                "Enable device admin permission"
            ) {

                val intent =
                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)

                intent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    compName
                )

                startActivity(intent)
            }

            return
        }

        // All permissions granted
        openMainInterface()
    }

    private fun openMainInterface() {
        setContentView(R.layout.activity_main)
    }

    private fun showPopup(
        title: String,
        message: String,
        onYes: () -> Unit
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("YES") { _, _ ->
                onYes()
            }
            .show()
    }

    private fun isAccessibilityEnabled(): Boolean {

        val expected =
            "$packageName/.MyAccessibilityService"

        val enabled =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

        return enabled.contains(expected)
    }

    private fun hasMediaPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= 33) {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getMediaPermissions(): Array<String> {

        return if (Build.VERSION.SDK_INT >= 33) {

            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

        } else {

            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private val requestMediaPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            checkPermissions()
        }
}
