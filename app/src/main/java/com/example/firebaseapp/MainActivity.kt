package com.example.firebaseapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.firebaseapp.api.createUser
import com.example.firebaseapp.auth.ProfileActivity
import com.example.firebaseapp.base.BaseActivity
import com.example.firebaseapp.views.showSnackBar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_main.*


private const val RC_SIGN_IN: Int = 123

class MainActivity : BaseActivity() {

    private var coordinatorLayout: ConstraintLayout? = null
    private var buttonLogin: Button? = null

    override val fragmentLayout: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout)
        buttonLogin = findViewById(R.id.main_activity_button_login)
        supportActionBar!!.hide()
        this.onClickLoginButton()
      //  this.onClickChatButton()
    }

    override fun onResume() {
        super.onResume()
        if (this.isCurrentUserLogged()) {
            this.startProfileActivity()
        }
       // this.updateUIWhenResuming()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponseAfterSignIn(requestCode, resultCode, data)
    }

    private fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {//SUCCESS
                //create user in FIRESTORE
                this.createUserInFirestore()
                // showSnackBar(coordinatorLayout!!, getString(R.string.connection_succeed))
            } else {//ERRORS
                when {
                    response == null -> showSnackBar(
                        coordinatorLayout!!,
                        getString(R.string.error_authentication_canceled)
                    )
                    response!!.error!!.errorCode == ErrorCodes.NO_NETWORK -> showSnackBar(
                        coordinatorLayout!!,
                        getString(R.string.error_no_internet)
                    )
                    response!!.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR -> showSnackBar(
                        coordinatorLayout!!,
                        getString(R.string.error_unknown_error)
                    )
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

   /* private fun onClickChatButton() {
        main_activity_button_chat.setOnClickListener {
            if (this.isCurrentUserLogged()) {
                this.startMentorChatActicity()
            } else {
                showSnackBar(this.coordinatorLayout!!, getString(R.string.error_not_connected))
            }
        }
    }*/

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
                .setLogo(R.drawable.ic_digital_library)
                .build(), RC_SIGN_IN
        )
    }

    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun updateUIWhenResuming() {
        this.buttonLogin!!.setText(
            if (this.isCurrentUserLogged()) getString(R.string.button_login_text_logged) else getString(
                R.string.button_login_text_not_logged
            )
        )
    }

    // --------------------
    // REST REQUEST when disconnect and reconnect
    // --------------------
    private fun createUserInFirestore() {
        if (this.getCurrentUser() != null) {
            createUser(
                this.getCurrentUser()!!.uid,
                this.getCurrentUser()!!.displayName!!,
                this.getCurrentUser()!!.photoUrl.toString()
            )
                .addOnFailureListener(this.onFailureListener())
        }
        Log.e("MainActivity", "getCurrentuser displayname==> ${getCurrentUser()!!.displayName}")
    }
}
