package com.example.blinkitclone.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitclone.R
import com.example.blinkitclone.adapter.AdapterCategoryProduct

import com.example.blinkitclone.databinding.FragmentCategoryProductBinding
import com.example.blinkitclone.databinding.ItemViewProductBinding
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.roomdb.CartProducts
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class CategoryProductFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var category: String? = null
    private lateinit var binding: FragmentCategoryProductBinding
    private var cartListener: CartListener? = null
    private lateinit var adapterCategoryProduct: AdapterCategoryProduct

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryProductBinding.inflate(layoutInflater)

        setStatusBarColor()
        settingToolBarTitle()

        gettingCategoryProducts()

        return binding.root
    }

    private fun gettingCategoryProducts() {
        lifecycleScope.launch {
            viewModel.fetchingAllProducts(category!!)
                .collect { productList ->
                    Log.d("test" , "gettingCategoryProducts ${productList.toString()}")
                    if (productList.isEmpty()) {
                        binding.rvProducts.visibility = View.GONE
                        binding.tvText.visibility = View.VISIBLE
                    } else {
                        binding.rvProducts.visibility = View.VISIBLE
                        binding.tvText.visibility = View.GONE
                    }
                    adapterCategoryProduct = AdapterCategoryProduct(::onItemViewCLick, ::onAddButtonClick , ::onIncrementButtonClick ,::onDecrementButtonClick)
                    binding.rvProducts.adapter = adapterCategoryProduct
                    adapterCategoryProduct.differ.submitList(productList)
                    Log.d("test" , "submitList ${productList.toString()}")
                    binding.shimmerViewContainer.visibility = View.GONE
                }
        }
    }

    private fun settingToolBarTitle() {
        val bundle = arguments
        category = bundle?.getString("category")
        binding.tbSearchFragment.title = category
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

    private var totalCount = 0

    private  fun onAddButtonClick(product: Product, productBinding: ItemViewProductBinding) {

        productBinding.apply {
            tvAdd.visibility = View.GONE   // add gone
            llProductCount.visibility = View.VISIBLE // count visible
        }

        val itemCount =  productBinding.tvProductCount.text.toString().toInt()+ 1
        productBinding.tvProductCount.text = itemCount.toString()
        totalCount += itemCount


        cartListener?.savingTotalItemInSp(1)
        cartListener?.gettingTotalItemInTheCart(true)
        cartListener?.showingCartItemCount(1.toString())

        product.itemCount = itemCount.toString()
        lifecycleScope.launch {
            val productSaved = async { saveProductInRoom(product) }
            Log.d("uid" , product.storeOwnerUid.toString())
            val saveProductInFirebase = async { updateProductInFirebase(product,itemCount.toString()) }
            productSaved.await()
            saveProductInFirebase.await()
        }

        //increment button
        productBinding.tvIncrementCount.setOnClickListener {
            onIncrementButtonClick(product,productBinding)
        }
        //decrement button
        productBinding.tvDecrementCount.setOnClickListener {
            onDecrementButtonClick(product,productBinding)
        }
    }

    private fun onIncrementButtonClick(product: Product , productBinding: ItemViewProductBinding){
        var itemCountInc =  productBinding.tvProductCount.text.toString().toInt()
        totalCount += 1
        itemCountInc++
        if (product.productStock!!.toInt() + 1 > itemCountInc) {   // in fb itemCount == 14 and stock == 15

            product.itemCount = itemCountInc.toString()

            lifecycleScope.launch {
                cartListener?.savingTotalItemInSp(1)
//                cartListener?.gettingTotalItemInTheCart()
                product.itemCount = itemCountInc.toString()
                val productSaved = async { saveProductInRoom(product) }
                val saveProductInFirebase = async { updateProductInFirebase(product,itemCountInc.toString()) }
                productSaved.await()
                saveProductInFirebase.await()
            }
            productBinding.tvProductCount.text = itemCountInc.toString()
            cartListener?.showingCartItemCount(1.toString())
        } else {
            Utils.showToast(requireContext(), "Can't add more item of this")
        }

    }

    private fun onDecrementButtonClick(product: Product , productBinding: ItemViewProductBinding){
        var itemCountInc =  productBinding.tvProductCount.text.toString().toInt()
        Log.d("iii" ,itemCountInc.toString())
        totalCount -= 1
        itemCountInc--
        lifecycleScope.launch {
            product.itemCount = itemCountInc.toString()
            cartListener?.savingTotalItemInSp(-1)
            cartListener?.showingCartItemCount("-1")
            val productSaved = async { saveProductInRoom(product) }
            val saveProductInFirebase = async { updateProductInFirebase(product,itemCountInc.toString()) }
            productSaved.await()
            saveProductInFirebase.await()
        }

        if (itemCountInc > 0) {
            Log.d("itemCountInc" , "if $itemCountInc")
            productBinding.tvProductCount.text = itemCountInc.toString()
        }
        else if (itemCountInc == 0 && totalCount != 0) {
            cartListener?.showingCartItemCount("0")
            Log.d("itemCountInc" , "else if1 $totalCount")
            lifecycleScope.launch { deleteProductInTheCart(product.productRandomId!!) }
            cartListener?.gettingTotalItemInTheCart(true)
            productBinding.tvProductCount.text = 0.toString()
            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.llProductCount.visibility = View.GONE
        }
        else if (totalCount == 0) {
            Log.d("itemCountInc" , "else if2")
            cartListener?.savingTotalItemInSp(0)
            lifecycleScope.launch { deleteProductInTheCart(product.productRandomId!!) }
            cartListener?.gettingTotalItemInTheCart(true)
            productBinding.tvProductCount.text = 0.toString()
            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.llProductCount.visibility = View.GONE
        }
    }

    private suspend fun deleteProductInTheCart(randomId: String) {
        viewModel.deleteProductInCart(randomId)
    }

    private suspend fun saveProductInRoom(product: Product) {
        val itemCount = product.itemCount.toString().toInt()
        val cartProduct = CartProducts(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity + product.productUnit,
            productPrice = "₹" + product.productPrice,
            productCount = itemCount,
            productStock = product.productStock?.toInt(),
            productImage = product.productImageUris?.get(0)!!,
            productCategory = product.productCategory,
            storeOwnerUid = product.storeOwnerUid
        )
        viewModel.insertProductInCart(cartProduct)
    }

    private fun updateProductInFirebase(product: Product, itemCount: String){
        viewModel.updateProductInFirebase(product,itemCount)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CartListener) {
            cartListener = context
        } else {
            throw ClassCastException("$context must implement CartVisibilityListener")
        }
    }

    private fun onItemViewCLick(product: Product) {

    }

}