package com.udacity.project4.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


fun isBackgroundLocationPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    ) {
        PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    } else {
        true
    }

}

private fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun isAllLocationPermissionsGranted(context: Context): Boolean {
    return isLocationPermissionGranted(context) &&
            isBackgroundLocationPermissionGranted(context)
}

fun Fragment.register(whatToDo: () -> Unit, rationale: () -> Unit): ActivityResultLauncher<String> {

    val x = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in the app.
            whatToDo()
            Log.d("askForPermissions", "good, permission granted ")
        } else {

            Log.d("askForPermissions", "Ooops, permission not granted ")
            //show educational message if necessary
            rationale()
        }
    }
    return x
}

@SuppressLint("MissingPermission")
fun askForPermissions(requestLocationPermissionLauncher: ActivityResultLauncher<String>) {


    requestLocationPermissionLauncher.launch(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

@SuppressLint("MissingPermission")
fun Fragment.checkPermissions(
    requestLocationPermissionLauncher: ActivityResultLauncher<String>, whatToDo: () -> Unit
) {
    if (isAllLocationPermissionsGranted(requireContext())) {

        whatToDo()
    } else {
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
//        requestLocationPermissionLauncher.launch(
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )

        askForPermissions(requestLocationPermissionLauncher)
    }
}