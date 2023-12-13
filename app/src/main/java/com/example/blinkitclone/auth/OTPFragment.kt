package com.example.blinkitclone.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.actvity.UserMainActivity
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.databinding.FragmentOTPBinding
import com.example.blinkitclone.viewmodels.AuthViewModel
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch



class OTPFragment : Fragment() {

    private lateinit var binding : FragmentOTPBinding
    private  var userNumber : String ? = null
    private val mainViewModel : AuthViewModel by viewModels()
    private  var verificationId : String ? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOTPBinding.inflate(layoutInflater)
        setStatusBarColor()
        customizingEnteringOTP()

        backingToSignInFragment()

        receiveAndShowUserNumber()

        sendOTP(userNumber)

        onLogin()

        return binding.root
    }

    private fun onLogin() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext()  , "Signing you...")

            val editTexts = arrayOf(
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
            )
            val otp = editTexts.joinToString("") { it.text.toString() }
            if (otp.length < editTexts.size) {
                Toast.makeText(requireContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show()
            } else {
                editTexts.forEach { it.text?.clear(); it.clearFocus() }
                verifyOTP(otp)
            }
        }
    }

    private fun verifyOTP(userOtp: String) {
        lifecycleScope.launch {mainViewModel.verificationId.collect{
            verificationId = it
        } }
        val credential = PhoneAuthProvider.getCredential(verificationId.toString(), userOtp)
        mainViewModel.apply {
            signInWithPhoneAuthCredential(credential , userNumber.toString())
            lifecycleScope.launch {
                isSignedInSuccessful.collect{
                    if(it==true){
                        Utils.hideDialog()
                        Utils.showToast(requireContext() , "Successfully Signed In")
                        startActivity(Intent(requireActivity()  , UserMainActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }
    }
    private fun sendOTP(userNumber: String?) {
        Utils.showDialog(requireContext() ,"Creating account...")
        mainViewModel.apply {
            sendOTP(userNumber!! , requireActivity())
            lifecycleScope.launch {
                otpSent.collect{
                    if(it == true){
                        Utils.hideDialog()
                        Utils.showToast(requireContext() , "OTP sent to your number")
                    }
                }
            }
        }
    }


    private fun backingToSignInFragment() {
        binding.tbOtpFragment.apply {
            setNavigationOnClickListener {
                findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
            }
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
            })
        }
    }

    private fun receiveAndShowUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("userNumber")
        binding.tvUserNumber.text = userNumber
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.white_yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}