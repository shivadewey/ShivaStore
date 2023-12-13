
package com.example.blinkitclone.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitclone.R
import com.example.blinkitclone.adapter.AdapterOrders
import com.example.blinkitclone.databinding.FragmentOTPBinding
import com.example.blinkitclone.databinding.FragmentOrdersBinding
import com.example.blinkitclone.models.OrderedItems
import com.example.blinkitclone.viewmodels.UserViewModel
import com.phonepe.intent.sdk.api.UserValidityCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.StringBuilder


class OrdersFragment : Fragment() {
    val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentOrdersBinding
    private lateinit var adapterOrders: AdapterOrders
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(LayoutInflater.from(requireContext()))

        lifecycleScope.launch { fetchingAllTheOrderedProducts()}
        return binding.root
    }

    private suspend fun fetchingAllTheOrderedProducts() {
        //first fetch the data then make a new dataclass and then send to rv
        viewModel.fetchingOrderedProducts().collect{orderLists ->
            Log.d("list" , orderLists.toString())
            if(orderLists.isNotEmpty()){
                val orderedLists = ArrayList<OrderedItems>()
                for(ordersList in orderLists){

                    val titles = StringBuilder()
                    var totalPrice  = 0
                    for (orders in ordersList.orderList!!){

                        titles.append("${orders.productCategory}, ")
                        val productPrice = orders.productPrice?.substring(1)?.toInt()!!
                        val productCount = orders.productCount!!
                        totalPrice += (productPrice * productCount)
                    }

                    val orderedItems = OrderedItems(ordersList.orderId,ordersList.orderDate,ordersList.orderStatus,titles.toString(),totalPrice)
                    orderedLists.add(orderedItems)
                }
                adapterOrders = AdapterOrders(requireContext())
                binding.rvOrders.adapter = adapterOrders
                adapterOrders.differ.submitList(orderedLists)
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }

}