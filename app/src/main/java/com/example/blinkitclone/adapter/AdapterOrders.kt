package com.example.blinkitclone.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.ItemViewOrdersBinding
import com.example.blinkitclone.models.OrderedItems
import com.example.blinkitclone.utils.Utils


class AdapterOrders(
    val context : Context
) : RecyclerView.Adapter<AdapterOrders.OrdersViewHolder>() {
    class OrdersViewHolder(val binding : ItemViewOrdersBinding) : ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<OrderedItems>(){
        override fun areItemsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem.orderId == newItem.orderId
        }
    }
    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(ItemViewOrdersBinding.inflate(LayoutInflater.from(parent.context) , parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            tvOrderDate.text = order.itemDate
            tvOrderTitles.text = order.itemTitle
            tvOrderAmount.text = "₹${order.itemPrice.toString()}"
            when(order.itemStatus){
                0 -> {
                    tvOrderStatus.text = "Ordered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context ,R.color.yellow)
                }
                1 -> {
                    tvOrderStatus.text = "Received"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context ,R.color.blue)
                }
                2 -> {
                    tvOrderStatus.text = "Dispatched"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context ,R.color.green)
                }
                3 -> {
                    tvOrderStatus.text = "Delivered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context ,R.color.orange)
                }
            }
        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("orderId",order.orderId.toString())
            bundle.putInt("orderStatus",order.itemStatus!!)
            Navigation.findNavController(it).navigate(R.id.action_ordersFragment_to_orderDetailFragment,bundle)
        }
    }

}