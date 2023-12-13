package com.example.blinkitclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkitclone.databinding.ItemViewProductBinding
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.utils.FilteringProducts


class AdapterCategoryProduct(
    val onItemViewCLick: (Product) -> Unit,
    val onAddButtonClick: (Product, ItemViewProductBinding) -> Unit,
    val onIncrementButtonClick: (Product, ItemViewProductBinding) -> Unit,
    val onDecrementButtonClick: (Product, ItemViewProductBinding) -> Unit,

    ) : Adapter<AdapterCategoryProduct.ProductsViewHolder>() ,Filterable{

    class ProductsViewHolder(val binding : ItemViewProductBinding) : ViewHolder(binding.root)


    private val diffUtils = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffUtils)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(ItemViewProductBinding.inflate(LayoutInflater.from(parent.context) ,parent ,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()

            val productImageList = product.productImageUris

            for(i in 0 until productImageList?.size!!){
                imageList.add(SlideModel(product.productImageUris?.get(i).toString()))
            }

            ivImageSlider.setImageList(imageList)

            tvProductTitle.text = product.productTitle

            val quantity = product.productQuantity + product.productUnit
            tvProductQuantity.text = quantity

            val price = "₹"+product.productPrice
            tvProductPrice.text = price

            if(product.itemCount?.toInt()!! > 0){
                tvAdd.visibility = View.GONE
                llProductCount.visibility = View.VISIBLE
                tvProductCount.text = product.itemCount
            }
            tvIncrementCount.setOnClickListener{onIncrementButtonClick(product,this)}
            tvDecrementCount.setOnClickListener{onDecrementButtonClick(product,this)}
            tvAdd.setOnClickListener {
                onAddButtonClick(product, this)
            }
        }
        holder.itemView.setOnClickListener {
            onItemViewCLick(product)
        }
    }

    private var filter : FilteringProducts?= null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {
        if(filter == null) return FilteringProducts(this,originalList)
        return filter as FilteringProducts
    }

}