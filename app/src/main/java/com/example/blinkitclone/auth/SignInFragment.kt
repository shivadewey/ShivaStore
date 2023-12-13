package com.example.blinkitclone.auth

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)

        setStatusBarColor()

        gettingUserNumber()

        navigateToOtpFragment()

        return binding.root
    }

    private fun navigateToOtpFragment() {
        binding.apply {
            btnContinue.setOnClickListener {
                val phoneNumber = binding.etUserNumber.text.toString()
                if(phoneNumber.isEmpty() || phoneNumber.length != 10){
                    Utils.showToast(requireContext() , "Please enter your valid mobile number")
                }else{
                    val bundle = Bundle()
                    bundle.putString("userNumber" , phoneNumber)
                    findNavController().navigate(R.id.action_signInFragment_to_OTPFragment , bundle)
                }

            }
        }
    }

    private fun gettingUserNumber() {
        binding.etUserNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s.toString().length
                if(len == 10){
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                }
                else{
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext() ,R.color.grayish_blue))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etUserNumber.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(number: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val len = number.toString().length
                    if (len == 10) {
                        val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
                        binding.btnContinue.setBackgroundColor(greenColor)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            }
        }
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