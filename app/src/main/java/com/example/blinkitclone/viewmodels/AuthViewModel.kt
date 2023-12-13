package com.example.blinkitclone.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blinkitclone.utils.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel(){

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId : StateFlow<String?> = _verificationId

    private val _isSignedInSuccessful  = MutableStateFlow(false)
    val  isSignedInSuccessful : StateFlow<Boolean?> = _isSignedInSuccessful

    private val _otpSent  = MutableStateFlow(false)
    val  otpSent : StateFlow<Boolean?> = _otpSent

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isSignedIn.value = true
        }
    }

    fun sendOTP(userNumber : String , activity: Activity){
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d("SHUBH", "onVerificationCompleted: ")
            }
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("SHUBH", "FirebaseException: ")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value = verificationId
                _otpSent.value = true
                Log.d("TAG" , "oncodesent$verificationId")

            }
        }
        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber("+1$userNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential , userNumber: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            Utils.getAuthInstance().signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // make one data class here
                        FirebaseDatabase.getInstance().getReference("Users").child(Utils.getCurrentUserUid()!!).child("userNumber").setValue(userNumber)
                        FirebaseDatabase.getInstance().getReference("Users").child(Utils.getCurrentUserUid()!!).child("userToken").setValue(token)
                            .addOnCompleteListener {
                                _isSignedInSuccessful.value = true
                            }
                    }
                }
        }

    }
}