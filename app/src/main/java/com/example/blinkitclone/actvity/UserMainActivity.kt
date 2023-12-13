package com.example.blinkitclone.actvity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.blinkitclone.adapter.AdapterCartProduct
import com.example.blinkitclone.databinding.ActivityUserMainBinding
import com.example.blinkitclone.databinding.BsCartProductsBinding
import com.example.blinkitclone.roomdb.CartProducts
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class UserMainActivity : AppCompatActivity() , CartListener {
    val viewModel : UserViewModel by viewModels()
    private lateinit var binding : ActivityUserMainBinding
    private lateinit var adapterCartProduct: AdapterCartProduct
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var cartProductList : List<CartProducts>
    private var totalCount  = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        settingVisibilityOfTheCartLayout()
        gettingTotalItemInTheCart(false)
        lifecycleScope.launch { getAllCartProducts() }
        goingToOrderFragment()
        showingDialog()
    }

    private fun goingToOrderFragment() {
        binding.btnNext.setOnClickListener {
//            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
//            val navController = navHostFragment.navController
//            setupActionBarWithNavController(navController)
            startActivity(Intent(this,OrderPlaceActivity::class.java))
//            setSupportActionBar()
        }

//        binding.btnNext.setOnClickListener {
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.apply {
//                replace(R.id.frameLayout , OrderProductFragment())
//                addToBackStack(null)
//            }.commit()

//            val navController = Navigation.findNavController(this,R.id.fragmentContainerView2)
//            NavigationUI.setupActionBarWithNavController(this, navController)
//            navController.navigate(R.id.action_categoryProductFragment2_to_orderProductFragment)
//            val action = CategoryProductFragmentDirections.actionCategoryProductFragment2ToOrderProductFragment()
//            findNavController(R.id.fragmentContainerView2).navigate(action)

    }


    override fun gettingTotalItemInTheCart(fromCategoryProduct : Boolean) {
        viewModel.fetchTotalItemInTheCart().observe(this){
            if(it.toInt() >    0){
                binding.llCart.visibility = View.VISIBLE
                if(!fromCategoryProduct)binding.tvNumberOfProductCount.text = it.toString()
            }
            else{
                binding.llCart.visibility = View.GONE
            }
        }
    }

    override fun showingCartItemCount(itemCount: String) {
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount.toInt()
        Log.d("updatedCount" , "updatedCount $itemCount")
        Log.d("updatedCount" , "updatedCount $previousCount")
        if(updatedCount < 0) binding.tvNumberOfProductCount.text ="0"

        binding.tvNumberOfProductCount.text = updatedCount.toString()
    }

    override fun savingTotalItemInSp(totalCount: Int) {
        viewModel.fetchTotalItemInTheCart().observe(this){
            val previousCount = it.toInt()
            viewModel.savingTotalItemInTheCart(totalCount + previousCount)
        }
    }

    override fun hideCartLayout() {
        Log.d("jjj", "dd")
        binding.tvNumberOfProductCount.text = "0"
        binding.llCart.visibility = View.GONE
    }

    private fun settingVisibilityOfTheCartLayout() {
        val sharedPreferences = getSharedPreferences("CartLayout" , MODE_PRIVATE)
        val itemCount = sharedPreferences.getString("ItemAdded" , 0.toString())
        if(itemCount?.toInt() != 0){
            binding.llCart.visibility = View.VISIBLE
        }
    }
    private suspend fun getAllCartProducts() {
       viewModel.getAllCartProducts().observe(this) {
           cartProductList = it as List<CartProducts>
       }
    }
    override fun onProductAddedToCart(shouldVisible : Boolean) {
        if (shouldVisible){

            binding.llCart.visibility = View.VISIBLE
        }
        else{
            binding.llCart.visibility = View.GONE
        }
    }

    override fun onSettingImageUri(imageUri: Uri) {
        Glide.with(this).load(imageUri).into(binding.ivProdudctImage)
    }
    private fun showingDialog() {
        binding.llItemCart.setOnClickListener {

            val cartBottomSheet = BsCartProductsBinding.inflate(LayoutInflater.from(this))
            val bs = BottomSheetDialog(this)
            bs.setContentView(cartBottomSheet.root)

            adapterCartProduct = AdapterCartProduct(this)
            cartBottomSheet.rvProductsItems.adapter = adapterCartProduct
            adapterCartProduct.differ.submitList(cartProductList)

            bs.show()

        }
    }
}