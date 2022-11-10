package com.udacity.project4.authentication

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser


class FakeFirebaseUserLiveData(fakeUser: FakeFirebaseUser) : MutableLiveData<FirebaseUser?>() {
    init {
        postValue(fakeUser)
    }
}