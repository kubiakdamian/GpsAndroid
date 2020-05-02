package pl.dmcs.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

class PermissionsService(private val context: Context, private val activity: AppCompatActivity) {

    fun requestForLocationPermission() {
        if (!checkIfLocationPermissionIsGranted()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
    }

    fun requestForInternetPermission() {
        if (!checkIfInternetPermissionIsGranted()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.INTERNET),
                102
            )
        }
    }

    fun checkGpsConnection() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("do you want to enable GPS to use application?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivity(
                    context,
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    null
                )
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun checkIfLocationPermissionIsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfInternetPermissionIsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }
}