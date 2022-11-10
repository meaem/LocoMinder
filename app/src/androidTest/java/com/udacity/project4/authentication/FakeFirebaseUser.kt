package com.udacity.project4.authentication

import android.net.Uri
import android.os.Parcel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.google.firebase.auth.MultiFactor
import com.google.firebase.auth.UserInfo

class FakeFirebaseUser : FirebaseUser() {
    override fun writeToParcel(p0: Parcel?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun getPhotoUrl(): Uri? {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): String? {
        TODO("Not yet implemented")
    }

    override fun getEmail(): String? {
        TODO("Not yet implemented")
    }

    override fun getPhoneNumber(): String? {
        TODO("Not yet implemented")
    }

    override fun getProviderId(): String {
        TODO("Not yet implemented")
    }

    override fun getUid(): String {
        TODO("Not yet implemented")
    }

    override fun isEmailVerified(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMetadata(): FirebaseUserMetadata? {
        TODO("Not yet implemented")
    }

    override fun getMultiFactor(): MultiFactor {
        TODO("Not yet implemented")
    }

    override fun getTenantId(): String? {
        TODO("Not yet implemented")
    }

    override fun getProviderData(): MutableList<out UserInfo> {
        TODO("Not yet implemented")
    }

    override fun isAnonymous(): Boolean {
        TODO("Not yet implemented")
    }

    override fun zza(): FirebaseApp {
        TODO("Not yet implemented")
    }

    override fun zzb(): FirebaseUser {
        TODO("Not yet implemented")
    }

    override fun zzc(p0: MutableList<Any?>): FirebaseUser {
        TODO("Not yet implemented")
    }

    override fun zzd(): com.google.android.gms.internal.`firebase-auth-api`.zzzy {
        TODO("Not yet implemented")
    }

    override fun zze(): String {
        TODO("Not yet implemented")
    }

    override fun zzf(): String {
        TODO("Not yet implemented")
    }

    override fun zzg(): MutableList<Any?>? {
        TODO("Not yet implemented")
    }

    override fun zzh(p0: com.google.android.gms.internal.`firebase-auth-api`.zzzy) {
        TODO("Not yet implemented")
    }

    override fun zzi(p0: MutableList<Any?>) {
        TODO("Not yet implemented")
    }
}