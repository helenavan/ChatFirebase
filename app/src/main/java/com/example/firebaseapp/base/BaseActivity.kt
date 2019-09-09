package com.example.firebaseapp.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseapp.R
import com.google.android.gms.tasks.OnFailureListener

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseActivity: AppCompatActivity() {

    abstract val fragmentLayout:Int

    protected fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    protected fun isCurrentUserLogged():Boolean{return (this.getCurrentUser() != null)}
    // --------------------
    // LIFE CYCLE
    // --------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(this.fragmentLayout)

    }
    // --------------------
    // UI
    // --------------------
    protected fun configureToolbar(titleAb:String) {
        val ab = supportActionBar
        ab!!.setDisplayHomeAsUpEnabled(false)
        ab!!.title = titleAb
    }

    // --------------------
    // ERROR HANDLER
    // --------------------
    protected fun onFailureListener(): OnFailureListener {
        return OnFailureListener { Toast.makeText(applicationContext,getString(R.string.error_unknown_error),
            Toast.LENGTH_SHORT).show() }
    }
}