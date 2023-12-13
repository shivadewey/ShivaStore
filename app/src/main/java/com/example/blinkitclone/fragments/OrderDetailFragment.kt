package com.example.blinkitclone.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitclone.R
import com.example.blinkitclone.adapter.AdapterCartProduct
import com.example.blinkitclone.databinding.FragmentOrderDetailBinding
import com.example.blinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {
    val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentOrderDetailBinding
    private lateinit var adapterCartProduct: AdapterCartProduct
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)

        val bundle = arguments
        val orderId = bundle?.getString("orderId")
        val orderStatus = bundle?.getInt("orderStatus")

        showStatus(orderStatus)
        lifecycleScope.launch { getTheOrderedItems(orderId) }

        return binding.root
    }

    private fun showStatus(orderStatus: Int?) {
        when(orderStatus){
            0 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
            1 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
            2 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv3.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
            3 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv3.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv4.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view3.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
        }
    }

    private suspend fun getTheOrderedItems(orderId: String?) {
        viewModel.getOrderedItems(orderId!!).collect{orderedItems->
            if(orderedItems!!.isNotEmpty()){
                adapterCartProduct = AdapterCartProduct(requireContext())
                binding.rvProductsItems.adapter = adapterCartProduct
                adapterCartProduct.differ.submitList(orderedItems)
            }
        }
    }

}