package com.udacity.project4.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

//import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val TAG = RemindersActivity::class.java.simpleName

    private lateinit var databinding: ActivityRemindersBinding
    private val viewModel: AuthenticationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = ActivityRemindersBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_reminders)


        setContentView(databinding.root)

//        viewModel.authenticationState.observe(this, Observer { authenticationState ->
//            when (authenticationState) {
//                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
////                    Log.d(TAG, "Success user name: '${user?.displayName}'")
//                    val intent = Intent(this, AuthenticationActivity::class.java)
//                    startActivity(intent)
//
//                    finish()
//                }
//                else -> Log.e(
//                    AuthenticationActivity.TAG,
//                    "Authentication state that doesn't require any UI change $authenticationState"
//                )
//            }
//        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                databinding.navHostFragment.findNavController().popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    @SuppressLint("MissingSuperCall")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        Log.d(TAG, "Activity onRequestPermissionsResult")
//        Log.d(TAG, "Activity permissions:$permissions")
//        Log.d(TAG, "Activity grantResults: $grantResults")
//
//    }
}
