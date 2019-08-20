package com.example.firebaseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.firebaseapp.auth.ProfileActivity
import com.example.firebaseapp.base.BaseActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val RC_SIGN_IN: Int = 123
    private var coordinatorLayout:CoordinatorLayout? = null

    override val fragmentLayout: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.onClickLoginButton()

    }

    override fun onResume() {
        super.onResume()
        this.updateUIWhenReming()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponseAfterSignIn(requestCode,resultCode,data!!)
    }

    //show Snack Bar with a message
    private fun showSnackBar(coordinatorLayout: CoordinatorLayout, message:String){
        Snackbar.make(coordinatorLayout,message,Snackbar.LENGTH_INDEFINITE)
    }

    private fun handleResponseAfterSignIn(requestCode:Int, resultCode: Int,data:Intent){
        val response = IdpResponse.fromResultIntent(data)
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RC_SIGN_IN){
               showSnackBar(coordinatorLayout!!,getString(R.string.connection_succeed))
            }else{
                if(response == null){
                    showSnackBar(coordinatorLayout!!,getString(R.string.error_authentication_canceled))
                }else if( response.errorCode == ErrorCodes.NO_NETWORK){
                    showSnackBar(coordinatorLayout!!, getString(R.string.error_no_internet))
                }else if (response.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(coordinatorLayout!!, getString(R.string.error_unknown_error))
                }
            }
        }
    }

    private fun onClickLoginButton() {
        main_activity_button_login.setOnClickListener {
                Toast.makeText(applicationContext, "click!", Toast.LENGTH_SHORT).show()
            if(this.isCurrentUserLogged()){
                this.startProfileActivity()
            }else{
                startSignInActivity()
            }
        }
    }

    private fun startSignInActivity() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(
                    mutableListOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())
                )
                .setIsSmartLockEnabled(false, true)
                .setLogo(R.drawable.ic_logo_auth)
                .build(), RC_SIGN_IN
        )
    }

    private fun startProfileActivity(){
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun updateUIWhenReming(){
       if(this.isCurrentUserLogged()){
           this.main_activity_button_login.text = getString(R.string.button_login_text_logged)
       }else{
           this.main_activity_button_login.text = getString(R.string.button_login_text_not_logged)
       }
    }
}
