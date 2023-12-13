package com.example.blinkitclone.utils

import android.widget.Filter

import com.example.blinkitclone.adapter.AdapterCategoryProduct
import com.example.blinkitclone.models.Product
import java.util.Locale

class FilteringProducts(
    private val adapter : AdapterCategoryProduct,
    private val filterList : ArrayList<Product>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            val query = constraint.toString().trim().uppercase(Locale.getDefault())
            val queryKeyword = query.split(" ")
            val filteredProducts = ArrayList<Product>()

            for (products in filterList) {
                if (queryKeyword.any { queryKeyword ->
                        products.productTitle?.uppercase(Locale.getDefault())
                            ?.contains(queryKeyword) == true ||
                                products.productCategory?.uppercase(Locale.getDefault())
                                    ?.contains(queryKeyword) == true ||
                                products.productType?.uppercase(Locale.getDefault())
                                    ?.contains(queryKeyword) == true ||
                                products.storeType?.uppercase(Locale.getDefault())
                                    ?.contains(queryKeyword) == true
                    }) {
                    filteredProducts.add(products)
                }
            }

            result.apply {
                count = filteredProducts.size
                values = filteredProducts
            }

        }
        else {
            result.apply {
                count = filterList.size
                values = filterList
            }
        }

        return result
    }

    override fun publishResults(p0: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as ArrayList<Product>)
    }
}