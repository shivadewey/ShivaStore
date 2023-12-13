package com.example.blinkitclone.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.adapter.AdapterBestsellers
import com.example.blinkitclone.adapter.AdapterCategories
import com.example.blinkitclone.adapter.AdapterCategoryProduct
import com.example.blinkitclone.databinding.BsSeeAllBinding
import com.example.blinkitclone.databinding.FragmentHomeBinding
import com.example.blinkitclone.databinding.ItemViewProductBinding
import com.example.blinkitclone.models.Categories
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.models.ProductType
import com.example.blinkitclone.roomdb.CartProducts
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.utils.Constansts
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentHomeBinding
    private lateinit var adapterBestsellers: AdapterBestsellers
    private lateinit var adapterCategoryProduct: AdapterCategoryProduct
    private var cartListener: CartListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        setStatusBarColor()
        lifecycleScope.launch { fetchingBestsellersProducts() }
        goingToProfileFragment()
        goingToSearchFragment()
        changeStatusAndAppBarColorWhileCollapsing()

        settingAllCategories()

        return binding.root
    }


    private suspend fun fetchingBestsellersProducts() {
        viewModel.fetchProductTypes().collect{
            Log.d("gggg",it.toString())

            adapterBestsellers = AdapterBestsellers(::onSeeAllButtonClick)
            binding.rvBestsellers.adapter = adapterBestsellers
            adapterBestsellers.differ.submitList(it)
        }
    }
    private fun goingToSearchFragment() {
        binding.searchCv.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }
    private fun goingToProfileFragment() {
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }
    private fun settingAllCategories() {
        val categoriesList = ArrayList<Categories>()
        for(i in 0 until Constansts.allProductsCategory.size){
            categoriesList.add(Categories(Constansts.allProductsCategory[i] , Constansts.allProductsCategoryIcon[i]))
        }
        binding.rvCategories.adapter = AdapterCategories(requireContext() , categoriesList, ::onCategoryItemClicked)

    }
    private fun changeStatusAndAppBarColorWhileCollapsing() {

        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val offsetFactor = 1 - (verticalOffset.toFloat() / totalScrollRange.toFloat())
            val statusBarColor = if (offsetFactor == 1f) ContextCompat.getColor(requireContext(),
                R.color.orange
            ) else ContextCompat.getColor(requireContext(), R.color.white) // Orange
            activity?.window?.statusBarColor = statusBarColor
            binding.collapsingToolbarLayout.setContentScrimColor(statusBarColor)
        }

    }
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.orange)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
    private fun onCategoryItemClicked(category : Categories){
        val bundle = Bundle()
        bundle.putString("category" , category.category)
        findNavController().navigate(R.id.action_homeFragment_to_categoryProductFragment2,bundle)
    }

    private fun onSeeAllButtonClick(productType: ProductType){
        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = BottomSheetDialog(requireContext())
        dialogView.setContentView(bsSeeAllBinding.root)

        val productList = ArrayList<Product>()

        for(i in 0 until productType.products?.size!!){
            productList.add(productType.products[i])
        }
        adapterCategoryProduct = AdapterCategoryProduct(::onItemViewCLick, ::onAddButtonClick , ::onIncrementButtonClick ,::onDecrementButtonClick)
        bsSeeAllBinding.rvProducts.adapter = adapterCategoryProduct
        adapterCategoryProduct.differ.submitList(productList)
        Log.d("test" , "submitList ${productList.toString()}")

        dialogView.show()
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
    private fun onIncrementButtonClick(product: Product, productBinding: ItemViewProductBinding){
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
    private fun onDecrementButtonClick(product: Product, productBinding: ItemViewProductBinding){
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
    private fun onItemViewCLick(product: Product) {}
}