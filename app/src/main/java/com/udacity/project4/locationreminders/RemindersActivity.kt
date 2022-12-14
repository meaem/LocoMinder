package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val TAG = RemindersActivity::class.java.simpleName

    private lateinit var databinding: ActivityRemindersBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val authenticationViewModel: AuthenticationViewModel by viewModel()
    private val _viewModel: SaveReminderViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = ActivityRemindersBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_reminders)


        setContentView(databinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

// Todo enable this block before submission
        authenticationViewModel.authenticationState.observe(this) { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
//                    Log.d(TAG, "Success user name: '${user?.displayName}'")
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    startActivity(intent)

                    finish()
                }
                else -> Log.d(
                    AuthenticationActivity.TAG,
                    "Authentication state that doesn't require any UI change $authenticationState"
                )
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                databinding.navHostFragment.findNavController().popBackStack()
                return true
            }
            R.id.logout -> {
                Log.d(TAG, "logout")
                authenticationViewModel.logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, "onActivityResult from Activity")
//
//
//        when (requestCode) {
//            REQUEST_TURN_DEVICE_LOCATION_ON -> when (resultCode) {
//                Activity.RESULT_OK -> checkDeviceLocationSettings(this,
//                    { _viewModel.locationServiceEnabled.value = true },
//                    {})
//            }
//        }
//
//    }

}
