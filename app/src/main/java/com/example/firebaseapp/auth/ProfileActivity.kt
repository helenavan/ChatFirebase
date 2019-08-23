package com.example.firebaseapp.auth

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.R
import com.example.firebaseapp.api.UserHelper
import com.example.firebaseapp.api.UserHelper.Companion.deleteUser
import com.example.firebaseapp.api.UserHelper.Companion.getUser
import com.example.firebaseapp.api.UserHelper.Companion.updateIsMentor
import com.example.firebaseapp.api.UserHelper.Companion.updateUsername
import com.example.firebaseapp.base.BaseActivity
import com.example.firebaseapp.models.User
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.view.*




private const val SIGN_OUT_TASK: Int = 10
private const val DELETE_USER_TASK = 20
private const val UPDATE_USERNAME: Int = 30

class ProfileActivity : BaseActivity() {

    private var textInputEditTextUsername: EditText? = null

    override val fragmentLayout: Int
        get() = R.layout.activity_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textInputEditTextUsername = findViewById(R.id.profile_activity_edit_text_username)
        this.onClickDeleButton()
        this.onClickSignOutButton()
        this.onClickUpdateButton()
        this.updateUIWhenCreating()
        this.onClickCheckBoxMentor()
    }

    private fun onClickUpdateButton() {
        profile_activity_button_update.setOnClickListener {
            this.updateUsernameInFirebase()
        }

    }

    fun onClickCheckBoxMentor() {
        profile_activity_check_box_is_mentor.setOnClickListener {
            this.updateUserIsMentor()
        }

    }

    private fun onClickSignOutButton() {
        profile_activity_button_sign_out.setOnClickListener {
            this.sigOutUserFromFirebase()
        }

    }

    fun onClickDeleButton() {
        profile_activity_button_delete.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setMessage(R.string.popup_message_confirmation_delete_account)
            builder.setPositiveButton(R.string.popup_message_choice_yes, ({ dialog, which ->
                deleteUserFromFirebase()
            }))

            builder.setNegativeButton(R.string.popup_message_choice_no, null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

    }

    private fun updateUIWhenCreating() {
        Log.e("MainActivity", "UpdateUIWhenCreating ===>")
        var email: String? = null
        if (this.getCurrentUser() != null) {

            //Get picture URL from Firebase
            if (this.getCurrentUser()!!.photoUrl != null) {
                Glide.with(this)
                    .load(this.getCurrentUser()!!.photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_activity_imageview_profile)
            }

            //Get email & username from Firebase
            email =
                if (TextUtils.isEmpty(this.getCurrentUser()!!.email)) getString(R.string.info_no_email_found) else this.getCurrentUser()!!.email

            //Update views with data
            this.profile_activity_text_view_email.text = email

            // 5 - Get additional data from Firestore
            getUser(this.getCurrentUser()!!.uid)
                .addOnSuccessListener { documentSnapshot ->
                    val currentUser = documentSnapshot.toObject(User::class.java)
                    Log.e("MainActivity", "username ===>${currentUser!!.username}")
                    val username =
                        if (TextUtils.isEmpty(currentUser!!.username)) getString(R.string.info_no_username_found) else currentUser.username

                    this.profile_activity_check_box_is_mentor.isChecked = currentUser.isMentor!!
                    textInputEditTextUsername!!.setText(username)
                }
        }
    }

    private fun sigOutUserFromFirebase() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnSuccessListener(
                this,
                this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK)
            )
    }

    private fun deleteUserFromFirebase() {
        if (this.getCurrentUser() != null) {
            deleteUser(this.getCurrentUser()!!.uid).addOnFailureListener(this.onFailureListener())
            AuthUI.getInstance()
                .delete(this)
                .addOnSuccessListener(
                    this,
                    this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK)
                )
        }

    }

    private fun updateUsernameInFirebase() {
        this.profile_activity_progress_bar.visibility = View.VISIBLE
        val username: String = textInputEditTextUsername!!.text.toString()
        if (this.getCurrentUser() != null) {
            if (!username.isEmpty() && !username.equals(getString(R.string.info_no_username_found))) {
                updateUsername(
                    username,
                    getCurrentUser()!!.uid
                ).addOnFailureListener(this.onFailureListener())
                    .addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME))

            }
        }
    }

    private fun updateUserIsMentor() {
        if (this.getCurrentUser() != null) {
            updateIsMentor(
                this.getCurrentUser()!!.uid,
                profile_activity_check_box_is_mentor.isChecked
            ).addOnFailureListener(this.onFailureListener())
        }
    }

    //Create OnCompleteListener called after tasks ended
    private fun updateUIAfterRESTRequestsCompleted(origin: Int): OnSuccessListener<Void> {
        return OnSuccessListener<Void> {
            when (origin) {
                SIGN_OUT_TASK -> finish()
                DELETE_USER_TASK -> finish()
                UPDATE_USERNAME -> profile_activity_progress_bar.visibility = View.INVISIBLE
            }
        }
    }

}