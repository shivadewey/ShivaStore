package com.example.blinkitclone.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Utils {
    private var dialog : AlertDialog? = null

    fun showDialog(context: Context,  message: String){
        val progress = ProgressDialogBinding.inflate(LayoutInflater.from(context))
        progress.tvMessage.text = message
        dialog   = AlertDialog.Builder(context).setView(progress.root).setCancelable(false).create()
        dialog!!.show()
    }

    fun hideDialog(){
        dialog?.dismiss()
    }

    fun showToast(context: Context, message : String){
        Toast.makeText(context,message , Toast.LENGTH_SHORT).show()
    }

    private var firebaseInstance: FirebaseDatabase? = null
    private var firebaseAuthInstance: FirebaseAuth? = null

    fun getDatabaseInstance(): FirebaseDatabase {
        if (firebaseInstance == null) {
            firebaseInstance = FirebaseDatabase.getInstance()
        }
        return firebaseInstance!!
    }

    fun getAuthInstance(): FirebaseAuth {
        if (firebaseAuthInstance == null) {
            firebaseAuthInstance = FirebaseAuth.getInstance()
        }
        return firebaseAuthInstance!!
    }

    fun getCurrentUserUid() : String?{
        return getAuthInstance().currentUser?.uid
    }

    fun getRandomId() : String{
        return (1..25).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
    }

    fun getCurrentDate(): String? {
        val currentDate = LocalDate.now()
        // Format the current date as a string
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}