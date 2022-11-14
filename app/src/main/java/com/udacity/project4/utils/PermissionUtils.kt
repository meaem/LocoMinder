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


private fun isBackgroundLocationPermissionGranted(context: Context): Boolean {
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
    return fineAccessGranted(context) || coarseAccessGranted(context)
}

private fun coarseAccessGranted(context: Context) =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) ==
            PackageManager.PERMISSION_GRANTED

private fun fineAccessGranted(context: Context) =
    ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

fun isAllLocationPermissionsGranted(context: Context): Boolean {
    return isLocationPermissionGranted(context) &&
            isBackgroundLocationPermissionGranted(context)
}

fun Fragment.registerForeground(
    whatToDo: () -> Unit,
    rationale: () -> Unit
): ActivityResultLauncher<Array<String>> {

    val x = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val anyGranted = permissions.any { it.value }

        if (anyGranted) {
            Log.d("PermissionsUtils", "Foreground permissions has been granted")

            whatToDo()
        } else {
            Log.d("PermissionsUtils", "Ooops, Foreground permissions are not granted ")
            rationale()
        }
    }
    return x
}

fun Fragment.registerAll(
    whatToDo: () -> Unit,
    rationale: () -> Unit
): ActivityResultLauncher<Array<String>> {

    val x = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val allGranted = permissions.all { it.value }

        if (allGranted) {
            Log.d("PermissionsUtils", "All permissions has been granted")

            whatToDo()
        } else {
            Log.d("PermissionsUtils", "Ooops, all permissions are not granted ")
            rationale()
        }
    }
    return x
}


@SuppressLint("MissingPermission")
fun askForAllPermissions(requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>) {

    val perms = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    requestLocationPermissionLauncher.launch(perms.toTypedArray())
}

@SuppressLint("MissingPermission")
fun askForForegroundPermissions(requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>) {

    val perms = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    requestLocationPermissionLauncher.launch(perms)
}

@SuppressLint("MissingPermission")
fun Fragment.checkAllPermissions(
    requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>,
    whatToDo: () -> Unit
) {
    if (isAllLocationPermissionsGranted(requireContext())) {

        whatToDo()
    } else {
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
//        requestLocationPermissionLauncher.launch(
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )

        askForAllPermissions(requestLocationPermissionLauncher)
    }
}

@SuppressLint("MissingPermission")
fun Fragment.checkForegroundPermissions(
    requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>,
    whatToDo: () -> Unit
) {
    if (isLocationPermissionGranted(requireContext())) {

        whatToDo()
    } else {

        askForForegroundPermissions(requestLocationPermissionLauncher)
    }
}