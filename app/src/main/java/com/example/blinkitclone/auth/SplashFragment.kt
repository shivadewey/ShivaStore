package com.example.blinkitclone.auth
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.actvity.UserMainActivity
import com.example.blinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {
    private val mainViewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setStatusBarColor()

        showSplashWithCondition()

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }


    private fun showSplashWithCondition() {
        Handler(Looper.getMainLooper()).postDelayed({
             lifecycleScope.launch {
                 mainViewModel.isSignedIn.collect{isSignedIn->
                     if(isSignedIn){
                         startActivity(Intent(requireActivity()  , UserMainActivity::class.java))
                         requireActivity().finish()
                     }
                     else findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                 }
             }

        },2000)
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
        }
    }
}