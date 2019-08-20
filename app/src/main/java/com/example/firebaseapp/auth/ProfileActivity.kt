package com.example.firebaseapp.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.R
import com.example.firebaseapp.base.BaseActivity
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity() {
    override val fragmentLayout: Int
        get() = R.layout.activity_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.updateUIWhenCreating()

    }

    fun onClickUpdateButton() {

    }

    fun onClickSignOutButton() {

    }

    fun onClickDeleButton() {

    }


    private fun updateUIWhenCreating() {
        var email: String? = null
        var name: String? = null
        if (this.getCurrentUser() != null) {
            //Get picture URL from Firebase
            if (this.getCurrentUser()!!.photoUrl != null) {
                Glide.with(applicationContext).load(this.getCurrentUser()!!.photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_activity_imageview_profile)
            }
        }
        if (TextUtils.isEmpty(this.getCurrentUser()!!.email)) {
            getString(R.string.info_no_email_found)
        } else {
            email = this.getCurrentUser()!!.email
        }
        if (TextUtils.isEmpty(this.getCurrentUser()!!.displayName)) {
            getString(R.string.info_no_username_found)
        } else {
            name = this.getCurrentUser()!!.displayName
        }
        this.profile_activity_text_view_email.text = email
        val textInputEditTextUsername = findViewById<EditText>(R.id.profile_activity_edit_text_username)
        textInputEditTextUsername.text = Editable.Factory.getInstance().newEditable(name)
    }


}