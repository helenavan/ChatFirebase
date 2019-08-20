package com.example.firebaseapp.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    protected fun configureToolbar() {
        val ab = supportActionBar
        ab!!.setDisplayHomeAsUpEnabled(true)
    }
}