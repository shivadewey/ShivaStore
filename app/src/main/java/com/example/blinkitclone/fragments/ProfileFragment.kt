package com.example.blinkitclone.fragments


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.EditAddressLayoutBinding
import com.example.blinkitclone.databinding.FragmentProfileBinding
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        setStatusBarColor()
        onAddressBookClick()
        onOrdersClick()

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
        return binding.root
    }

    private fun onOrdersClick() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
    }


    private fun onAddressBookClick() {
        binding.llAddress.setOnClickListener {
            val editAddressLayoutBinding = EditAddressLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            viewModel.getUserAddress {
                editAddressLayoutBinding.etAddress.setText(it.toString())
            }
            val alertDialog =  AlertDialog.Builder(requireContext())
                .setView(editAddressLayoutBinding.root)
                .create()

            alertDialog.show()

            editAddressLayoutBinding.btnEdit.setOnClickListener {
                editAddressLayoutBinding.etAddress.isEnabled = true
            }
            editAddressLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveAddress(editAddressLayoutBinding.etAddress.text.toString())
                alertDialog.dismiss()
                Utils.showToast(requireContext() , "Saved changes!")
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