package com.example.blinkitclone.actvity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.blinkitclone.R
import com.example.blinkitclone.adapter.AdapterCartProduct
import com.example.blinkitclone.api.ApiUtilities
import com.example.blinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.blinkitclone.databinding.AddressLayoutBinding
import com.example.blinkitclone.models.Orders
import com.example.blinkitclone.roomdb.CartProducts
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.utils.Constansts
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.viewmodels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()

    private lateinit var binding: ActivityOrderPlaceBinding
    private var cartProductList = listOf<CartProducts>()
    private lateinit var adapterCartProduct: AdapterCartProduct
    private lateinit var b2BPGRequest: B2BPGRequest
    private var address = " "
    private var cartListener: CartListener? = null
    private var storeOwnerUid: String = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("s", "onCreate")
        setStatusBarColor()
        lifecycleScope.launch { getAllCartProducts() }

        initializePhonePay()

        onNextButtonClick()
//        viewModel.savingAddressStatus()
    }


    override fun onStart() {
        super.onStart()
        Log.d("s", "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("s", "onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.d("s", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("s", "onDestroy")
    }

    private fun initializePhonePay() {
        val data = JSONObject()
        PhonePe.init(this)
        data.put("merchantTransactionId", Constansts.merchantTransactionId)
        data.put("merchantId", Constansts.MERCHANT_ID)
        data.put("amount", 200)//Long. Mandatory
        data.put("mobileNumber", "8839990051") //String. Optional
        data.put("callbackUrl", "https://webhook.site/callback-ur") //String. Mandatory
        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")
        data.put("paymentInstrument", paymentInstrument)//OBJECT. Mandatory
        val deviceContext = JSONObject()
        deviceContext.put("deviceOS", "ANDROID")
        data.put("deviceContext", deviceContext)
//        val base64Body = android.util.Base64(Gson().toJson(data))
        val payloadBase64 = android.util.Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP
        )
        val checksum = sha256(payloadBase64 + Constansts.apiEndPoint + Constansts.salt) + "###1";

        Log.d("PAPAYACODERS", "onCreate: $payloadBase64")
        Log.d("PAPAYACODERS", "onCreate: ${Constansts.merchantTransactionId}")
        Log.d("PAPAYACODERS", "onCreate: $checksum")
        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constansts.apiEndPoint)
            .build()
    }

    private fun onNextButtonClick() {
        binding.btnNext.setOnClickListener {
            viewModel.gettingAddressStatus().observe(this) { status ->
                if (status) {
                    gettingPaymentView()
                } else {
                    val addressLayout = AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val addressDialog = AlertDialog.Builder(this)
                        .setView(addressLayout.root)
                        .create()
                    addressDialog.show()
                    addressLayout.etPinCode.focusable
                    addressLayout.btnAdd.setOnClickListener {
                        saveUserAddress(addressLayout, addressDialog)
                    }
                }
            }

        }
    }

    private fun saveUserAddress(addressLayout: AddressLayoutBinding, addressDialog: AlertDialog) {
        Utils.showDialog(this, "processing..")
        val userPinCode = addressLayout.etPinCode.text.toString()
        val userPhoneNumber = addressLayout.etPhoneNumber.text.toString()
        val userState = addressLayout.etState.text.toString()
        val userDistrict = addressLayout.etDistrict.text.toString()
        val userAddress = addressLayout.etDescriptiveAddress.text.toString()
        address = "$userPinCode,$userDistrict($userState),$userAddress,$userPhoneNumber"
        //saving address status
        lifecycleScope.launch { viewModel.saveAddress(address) }
        //saving address of the user
        lifecycleScope.launch {
            viewModel.savingAddressStatus()
        }
        addressDialog.dismiss()
        Utils.showToast(this, "Added your address")

        gettingPaymentView()

    }

    private fun savingOrderOfUser(address: String) {
        // add address when user added the address once
        viewModel.getAllCartProducts().observe(this) { cartProductsList ->

            if (!cartProductsList.isNullOrEmpty()) {
                storeOwnerUid = cartProductsList[0].storeOwnerUid!!
                Log.d("store", storeOwnerUid.toString())
                val order = Orders(
                    orderId = Utils.getRandomId(), orderList = cartProductsList,
                    userAddress = address, orderStatus = 0, orderDate = Utils.getCurrentDate(),
                    orderingUserId = Utils.getCurrentUserUid()
                )
                viewModel.savingOrderedProductsInFirebase(order)
                Log.d("store", storeOwnerUid)
                viewModel.sendNotification(
                    storeOwnerUid,
                    "Check please, some order has been sent",
                    "Ordered"
                )
            }

            for (products in cartProductsList) {
                viewModel.updatingProductsCounts(products)
            }
        }
        Utils.hideDialog()
    }

    private fun checkStatus() {

        val xVerify =
            sha256("/pg/v1/status/${Constansts.MERCHANT_ID}/${Constansts.merchantTransactionId}${Constansts.salt}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constansts.MERCHANT_ID,
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val res = ApiUtilities.getApiInterface()
                .checkStatus(Constansts.MERCHANT_ID, Constansts.merchantTransactionId, headers)
            withContext(Dispatchers.Main) {
                if (res.body() != null && res.body()!!.success) {
                    Utils.showToast(this@OrderPlaceActivity, res.body()!!.message)


                    savingOrderOfUser(address)


                    viewModel.deleteAllCartProducts()
                    viewModel.savingTotalItemInTheCart(0)
                    cartListener?.hideCartLayout()
                    startActivity(Intent(this@OrderPlaceActivity, UserMainActivity::class.java))
                    finish()
                } else {


                    Utils.showToast(this@OrderPlaceActivity, "Not Done")
                    Utils.showToast(this@OrderPlaceActivity, "Pay again please!")
                    Utils.hideDialog()
                }
            }
        }
    }

    private fun gettingPaymentView() {
        try {

            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator")
                ?.let {
                    startActivityForResult(it, 1)
                };
        } catch (e: PhonePeInitException) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            checkStatus()
        }
    }

    private suspend fun getAllCartProducts() {
        viewModel.getAllCartProducts().observe(this) {
            cartProductList = it as List<CartProducts>
            adapterCartProduct = AdapterCartProduct(this)
            binding.rvProductsItems.adapter = adapterCartProduct
            adapterCartProduct.differ.submitList(cartProductList)
            var totalAmount = 0
            for (products in cartProductList) {
                val productPrice = products.productPrice?.substring(1)?.toInt()!!
                val productCount = products.productCount!!
                totalAmount += (productPrice * productCount)
            }
            binding.tvSubTotal.text = "₹${totalAmount}"
            if (totalAmount > 200) {
                binding.tvGrandTotal.text = "₹${totalAmount}"
            } else {
                binding.tvDeliveryCharge.text = "₹15"
                val newAmount = totalAmount + 15
                binding.tvGrandTotal.text = "₹${newAmount}"
            }
        }
    }

    private fun setStatusBarColor() {
        window?.apply {
            val statusBarColors =
                ContextCompat.getColor(this@OrderPlaceActivity, R.color.white_yellow)
            statusBarColor = statusBarColors
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

//    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
//        outState.putString("address", address)
//        outState.putString("storeOwnerUid", storeOwnerUid)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        address = savedInstanceState.getString("address")!!
//        storeOwnerUid = savedInstanceState.getString("storeOwnerUid")!!
//    }

}