package com.udacity.project4.authentication

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.base.BaseViewModel

class AuthenticationViewModel(
    fbLiveData: LiveData<FirebaseUser?>,
    app: Application
) : BaseViewModel(app) {

    private var logoutLogic = fun() {
        AuthUI.getInstance()
            .signOut(getApplication())
            .addOnCompleteListener {

                if (!it.isSuccessful) {
                    showToast.value = "Could not logout!! Please try again"
                }
            }
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
        //, INVALID_AUTHENTICATION
    }

    fun logout() {
        logoutLogic()
    }

    @VisibleForTesting
    fun setLogoutLogic(logic: () -> Unit) {
        logoutLogic = logic
    }

    // Create an authenticationState variable based off the FirebaseUserLiveData object. By
    //  creating this variable, other classes will be able to query for whether the user is logged
    //  in or not

    val authenticationState = fbLiveData.map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}