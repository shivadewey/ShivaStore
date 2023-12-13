package com.example.blinkitclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.blinkitclone.databinding.ItemViewBestsellersBinding
import com.example.blinkitclone.models.ProductType

class AdapterBestsellers(
    val onSeeAllButtonClick: (ProductType) -> Unit
) : RecyclerView.Adapter<AdapterBestsellers.BestsellersViewHolder>() {
    class BestsellersViewHolder(val binding : ItemViewBestsellersBinding):ViewHolder(binding.root)

    val diffUtil = object : ItemCallback<ProductType>(){
        override fun areItemsTheSame(oldItem: ProductType, newItem: ProductType): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductType, newItem: ProductType): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestsellersViewHolder {
        return BestsellersViewHolder(ItemViewBestsellersBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestsellersViewHolder, position: Int) {
        val productType = differ.currentList[position]
        holder.binding.apply {
            tvCategory.text = productType.categoryName
            tvTotalProducts.text = productType.products?.size.toString() + " products"

            val ivLIst = listOf(ivProduct1,ivProduct2,ivProduct3)

            val minimumSize = minOf(ivLIst.size , productType.products?.size!!)

            for(i in 0 until minimumSize){
                ivLIst[i].visibility = View.VISIBLE
                Glide.with(holder.itemView).load(productType.products[i].productImageUris?.get(i)).into(ivLIst[i])
            }

            if(minimumSize == 3 && productType.products.size > 3){
                tvProductCount.visibility = View.VISIBLE
                tvProductCount.text = "+" + (productType.products.size - 3).toString()
            }
            else{

            }
            tvSeeALl.setOnClickListener { onSeeAllButtonClick(productType) }
        }
    }

}