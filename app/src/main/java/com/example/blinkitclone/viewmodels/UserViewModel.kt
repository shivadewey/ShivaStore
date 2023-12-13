package com.example.blinkitclone.viewmodels

import android.app.Application

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Index
import com.bumptech.glide.disklrucache.DiskLruCache.Value
import com.example.blinkitclone.api.ApiUtilities
import com.example.blinkitclone.models.Orders
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.models.ProductType
import com.example.blinkitclone.models.Stores
import com.example.blinkitclone.models.notification.Notification
import com.example.blinkitclone.models.notification.NotificationData
import com.example.blinkitclone.roomdb.CartProducts
import com.example.blinkitclone.roomdb.CartProductsDao
import com.example.blinkitclone.roomdb.CartProductsDatabase

import com.example.blinkitclone.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(private val application: Application) : AndroidViewModel(application) {
    private val cartDao : CartProductsDao = CartProductsDatabase.getDatabaseInstance(application).cartProductsDao()
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("MyPrefs", MODE_PRIVATE)
    suspend fun insertProductInCart(cartProducts: CartProducts){
        cartDao.insertCartProduct(cartProducts)
    }

    suspend fun deleteProductInCart(productId : String?){
        cartDao.deleteCartProduct(productId)
    }
    suspend fun updateProductInCart(cartProducts: CartProducts){
        cartDao.updateCartProduct(cartProducts)
    }

    fun getAllCartProducts() : LiveData<List<CartProducts>>{
        return cartDao.getAllProductsNotes()
    }
     suspend fun deleteAllCartProducts(){
        cartDao.deleteAll()
    }
    fun fetchingAllProducts(categories: String): Flow<ArrayList<Product>> = callbackFlow {
        val databaseRef = Utils.getDatabaseInstance().getReference("Admins/ProductCategory/${categories}/Products")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = ArrayList<Product>()
                for (products in snapshot.children) {
                    val product = products.getValue(Product::class.java)
                    if (categories == "All" || product?.productCategory == categories) {
                        product?.let { productList.add(it) }
                    }
                }
                Log.d("test" , productList.toString())
                trySend(productList)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        databaseRef.addValueEventListener(eventListener)  // single listner resolved the problem of add button coming multiple times
        awaitClose { databaseRef.removeEventListener(eventListener) }
    }

   fun updateProductInFirebase(product: Product,itemCount : String){
        Utils.getDatabaseInstance().getReference("Admins/ProductCategory/${product.productCategory}/Products/${product.productRandomId}").child("itemCount").setValue(itemCount)
        Utils.getDatabaseInstance().getReference("Admins/AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
       Utils.getDatabaseInstance().getReference("Admins/ProductType/${product.productType}/Products").child(product.productRandomId!!).child("itemCount").setValue(itemCount)
    }

    fun savingTotalItemInTheCart(totalCount: Int) {
        val editor = sharedPreferences.edit()
        editor.apply {
            editor.putString("TotalCount"  , totalCount.toString())
        }.apply()
    }

    fun fetchTotalItemInTheCart() : MutableLiveData<String> {
        val totalItemLiveData = MutableLiveData<String>()
        totalItemLiveData.value = sharedPreferences.getString("TotalCount", 0.toString())
        return totalItemLiveData
    }

    fun savingAddressStatus(){
        val editor = sharedPreferences.edit()
        editor.apply{
            editor.putBoolean("addressAdded" , true)
        }.apply()
    }

    fun gettingAddressStatus() : MutableLiveData<Boolean>{
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressAdded" , false)
        return status
    }

    fun saveAddress(address : String){
        FirebaseDatabase.getInstance().getReference("Users").child(Utils.getCurrentUserUid()!!).child("userAddress").setValue(address)
    }

    fun savingOrderedProductsInFirebase(orders: Orders){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orders.orderId!!).setValue(orders)
    }


    fun updatingProductsCounts(product : CartProducts){
        Utils.getDatabaseInstance().getReference("Admins/ProductCategory/${product.productCategory}/Products/${product.productId}").child("itemCount").setValue("0")
        val stockCount = product.productCount?.toInt()
            ?.let { product.productStock?.toInt()?.minus(it) }
        Utils.getDatabaseInstance().getReference("Admins/ProductCategory/${product.productCategory}/Products/${product.productId}").child("productStock").setValue(stockCount.toString())
    }



    fun getUserAddress(callback: (String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Utils.getCurrentUserUid()!!).child("userAddress")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Access the userAddress value
                    val userAddress = dataSnapshot.getValue(String::class.java)
                    callback(userAddress)
                } else {
                    callback(null) // or handle the case when userAddress does not exist
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null) // Handle the error case
            }
        })
    }


    fun fetchingOrderedProducts() : Flow<ArrayList<Orders>> = callbackFlow{

        val db = Utils.getDatabaseInstance().getReference("Admins/Orders").orderByChild("orderStatus")
        // save orders in users node also
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentOrderList = ArrayList<Orders>()
                for(orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)
                    if(order?.orderingUserId == Utils.getCurrentUserUid()){
                        currentOrderList.add(order!!)
                    }
                }
                trySend(currentOrderList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getOrderedItems(orderId : String) : Flow<List<CartProducts>?>  = callbackFlow {
        val db = Utils.getDatabaseInstance().getReference("Admins/Orders/${orderId}")
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartProducts = snapshot.getValue(Orders::class.java)
                trySend(cartProducts?.orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        db.addListenerForSingleValueEvent(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun sendNotification(storeOwnerUid : String , message : String, title : String){
        val getToken = Utils.getDatabaseInstance().getReference("Admins/Stores/${storeOwnerUid}/Store Information").get()
        getToken.addOnSuccessListener {
            val storeOwnerDetail = it.getValue(Stores::class.java)
            val token =  storeOwnerDetail?.storeOwnerToken
            Log.d("send" , token.toString())
            val notification  = Notification(token, NotificationData(title,message))
            ApiUtilities.notificationApi.sendNotification(notification).enqueue(object : Callback<Notification>{
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    Log.d("send" , "Notification sent")
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }
    }


    fun gettingAllProducts()  : Flow<ArrayList<Product>> = callbackFlow {
        val db = Utils.getDatabaseInstance().getReference("Admins/AllProducts")
        val eventListener = object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = ArrayList<Product>()
                for(products in snapshot.children){
                    val product = products.getValue(Product::class.java)
                    allProducts.add(product!!)
                }
                trySend(allProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}
    }

    fun fetchProductTypes(): Flow<List<ProductType>> = callbackFlow {
        val db = Utils.getDatabaseInstance().getReference("Admins/ProductType")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productCategories = ArrayList<ProductType>()
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key
                    val products = categorySnapshot.child("Products")
                    val productList = ArrayList<Product>()

                    for(allProducts in products.children){
                        val product = allProducts.getValue(Product::class.java)
                        productList.add(product!!)
                    }

                    val productCategory = ProductType(categoryName= categoryName, products = productList)
                    productCategories.add(productCategory)
                }
                trySend(productCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        db.addValueEventListener(eventListener)

        // Use awaitClose with a lambda for cleanup when the flow is cancelled
        awaitClose { db.removeEventListener(eventListener) }
    }

}