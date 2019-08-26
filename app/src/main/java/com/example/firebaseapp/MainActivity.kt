package com.example.firebaseapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.firebaseapp.api.UserHelper.Companion.createUser
import com.example.firebaseapp.auth.ProfileActivity
import com.example.firebaseapp.base.BaseActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

private const val RC_SIGN_IN: Int = 123

class MainActivity : BaseActivity() {

    private var coordinatorLayout: CoordinatorLayout? = null

    override val fragmentLayout: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout)
        this.onClickLoginButton()
        this.onClickChatButton()
    }

    override fun onResume() {
        super.onResume()
        this.updateUIWhenReming()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponseAfterSignIn(requestCode, resultCode, data)
    }

    //show Snack Bar with a message
    private fun showSnackBar(coordinatorLayout: CoordinatorLayout, message: String) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
    }

    private fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            var urlPic:String? = null
            if (resultCode == Activity.RESULT_OK) {//SUCCESS
                //create user in FIRESTORE
                createUser(this.getCurrentUser()!!.uid,this.getCurrentUser()!!.displayName!!, getCurrentUser()!!.photoUrl.toString()).addOnFailureListener(this.onFailureListener())
               // this.createUserInFirestore()
                showSnackBar(coordinatorLayout!!, getString(R.string.connection_succeed))
            } else {//ERRORS
                when {
                    response == null -> showSnackBar(coordinatorLayout!!, getString(R.string.error_authentication_canceled))
                    response!!.error!!.errorCode == ErrorCodes.NO_NETWORK -> showSnackBar(coordinatorLayout!!, getString(R.string.error_no_internet))
                    response!!.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR -> showSnackBar(coordinatorLayout!!, getString(R.string.error_unknown_error))
                }
            }
        }
    }

    private fun onClickLoginButton() {
        main_activity_button_login.setOnClickListener {
          //  Toast.makeText(applicationContext, "click!", Toast.LENGTH_SHORT).show()
            if (this.isCurrentUserLogged()) {
                this.startProfileActivity()
            } else {
                this.startSignInActivity()
            }
        }
    }

    private fun onClickChatButton() {
        main_activity_button_chat.setOnClickListener {
            if (this.isCurrentUserLogged()) {
                this.startMentorChatActicity()
            }else{
                //  this.showSnackBar(this.coordinatorLayout!!, getString(R.string.error_not_connected))
            }
        }
    }

    private fun startMentorChatActicity(){
        val intent:Intent = Intent(applicationContext, MentorChatActivity::class.java)
        startActivity(intent)
    }

    private fun startSignInActivity() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(
                    arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build()
                    )
                )
                .setIsSmartLockEnabled(false, true)
                .setLogo(R.drawable.ic_logo_auth)
                .build(), RC_SIGN_IN
        )
    }

    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun updateUIWhenReming() {
        if (this.isCurrentUserLogged()) {
            this.main_activity_button_login.text = getString(R.string.button_login_text_logged)
        } else {
            this.main_activity_button_login.text = getString(R.string.button_login_text_not_logged)
        }
    }

    // --------------------
    // REST REQUEST
    // --------------------
    private fun createUserInFirestore() {

        this.getCurrentUser()?.let{
            var urlPicture:String? = null
            if(getCurrentUser()!!.photoUrl != null){
                urlPicture = getCurrentUser()!!.photoUrl.toString()
            }

            val username = getCurrentUser()!!.displayName as String
            val uid = getCurrentUser()!!.uid as String
            Log.e("MainActivity", "ui : $uid"+"url : $urlPicture")
            createUser(uid=uid,username = username,urlPicture =  urlPicture!!)
                .addOnFailureListener { this.onFailureListener() }
        }

    }

}
