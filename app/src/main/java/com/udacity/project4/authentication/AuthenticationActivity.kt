package com.udacity.project4.authentication


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel.AuthenticationState.AUTHENTICATED
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private val viewModel: AuthenticationViewModel by viewModel()

    companion object {
        const val TAG = "AuthenticationActivity"
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
//        this.onSignInResult(it)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val databinding = ActivityAuthenticationBinding.inflate(layoutInflater)

        // Observe the authentication state so we can know if the user has logged in successfully.
        // If the user has logged in successfully, bring them back to the settings screen.
        // If the user did not log in successfully, display an error message.
        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                AUTHENTICATED -> {
//                    Log.d(TAG, "Success user name: '${user?.displayName}'")
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> Log.e(
                    TAG,
                    "Authentication state that doesn't require any UI change $authenticationState"
                )
            }
        })

        databinding.loginButton.setOnClickListener {
            launchSignInFlow()
        }

        setContentView(databinding.root)
    }

//    override fun onStart() {
//        super.onStart()
//        Log.d(TAG, "onStart")
//
//        val user = FirebaseAuth.getInstance().currentUser
//        Log.d(TAG, "onStart $user")
////        user?.let {
////            checkUserAndStartRemiderActivity(it)
////        }
//
//    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )


        val customLayout = AuthMethodPickerLayout.Builder(R.layout.auth_custom_layout_xml)
            .setGoogleButtonId(R.id.with_google)
            .setEmailButtonId(R.id.by_email)
//            .setTosAndPrivacyPolicyId(R.id.baz)

            .build()

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(com.udacity.project4.R.drawable.ic_location)
            .setAuthMethodPickerLayout(customLayout)
            .setTheme(R.style.Theme_Project4)
            .build()

        signInLauncher.launch(signInIntent)
    }


//    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//        val response = result.idpResponse
//        if (result.resultCode == RESULT_OK) {
//            // Successfully signed in
//            val user = FirebaseAuth.getInstance().currentUser
//            Log.d(TAG, "Success user name: '${user?.displayName}'")
//
////            checkUserAndStartRemiderActivity(user)
//            // ...
//        } else {
//            // Sign in failed. If response is null the user canceled the
//            // sign-in flow using the back button. Otherwise check
//            // response.getError().getErrorCode() and handle the error.
//            // ...
//            Toast.makeText(this, getString(R.string.invalid_login_message), Toast.LENGTH_SHORT)
//                .show()
//
//            if (response == null) {
//                Log.d(TAG, "Sign in failed response is null")
//
//            } else {
//                Log.d(TAG, "Sign in failed error code is:${response.getError()?.getErrorCode()} ")
//            }
//        }
//    }

//    private fun checkUserAndStartRemiderActivity(user: FirebaseUser?) {
//
//
//    }
}
