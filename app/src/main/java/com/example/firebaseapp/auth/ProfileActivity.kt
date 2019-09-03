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
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.R
import com.example.firebaseapp.api.*
import com.example.firebaseapp.base.BaseActivity
import com.example.firebaseapp.models.User
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.view.*


private const val SIGN_OUT_TASK: Int = 10
private const val DELETE_USER_TASK = 20
private const val UPDATE_USERNAME: Int = 30

class ProfileActivity : BaseActivity() {

    private var textInputEditTextUsername: EditText? = null
    private var imageProfil: ImageView? = null
    private val firestoreUser by lazy {
        FirebaseFirestore.getInstance().collection("users").document(getCurrentUser()!!.uid)
    }

    override val fragmentLayout: Int
        get() = R.layout.activity_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textInputEditTextUsername = findViewById(R.id.profile_activity_edit_text_username)
        imageProfil = findViewById(R.id.profile_activity_imageview_profile)
        this.onClickDeleButton()
        this.onClickSignOutButton()
        this.onClickUpdateButton()
       // this.updateUI()
        this.updateUIWhenCreating()
        //  this.onClickCheckBoxMentor()
    }

    private fun onClickUpdateButton() {
        profile_activity_button_update.setOnClickListener {
            this.updateUsernameInFirebase()
        }
    }

/*    fun onClickCheckBoxMentor() {
        profile_activity_check_box_is_mentor.setOnClickListener {
            this.updateUserIsMentor()
        }
    }*/

    private fun onClickSignOutButton() {
        profile_activity_button_sign_out.setOnClickListener {
            this.sigOutUserFromFirebase()
        }

    }

    private fun onClickDeleButton() {
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

        var email: String? = null
        if (this.getCurrentUser() != null) {
            Log.e(
                "ProfilActivity",
                "UpdateUIWhenCreating photo ===> ${getCurrentUser()!!.photoUrl}"
            )
            //Get picture URL from Firebase

            //Get email & username from Firebase
            email =
                if (TextUtils.isEmpty(this.getCurrentUser()!!.email)) getString(R.string.info_no_email_found) else this.getCurrentUser()!!.email

            //Update com.example.firebaseapp.views with data
            this.profile_activity_text_view_email.text = email

            // 5 - Get additional data from Firestore
            getUser(this.getCurrentUser()!!.uid)
                .addOnSuccessListener { documentSnapshot ->
                    val currentUser = documentSnapshot.toObject(User::class.java)
                    if (this.getCurrentUser()!!.photoUrl != null) {
                        Glide.with(applicationContext)
                            .load(currentUser!!.urlPicture)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_moustache480px)
                            .into(imageProfil!!)
                    } else {
                        imageProfil!!.setImageResource(R.drawable.ic_moustache480px)
                    }
                    //  Log.e("ProfilActivity", "username ===>${currentUser!!.username}")
                    Log.e(
                        "ProfilActivity",
                        "in getUser =>phot url ===> ${currentUser!!.urlPicture}"
                    )
                    val username =
                        if (TextUtils.isEmpty(currentUser!!.username)) getString(R.string.info_no_username_found) else currentUser.username
                    Log.e(
                        "ProfilActivity",
                        "DISPLAYNAME ===> ${getCurrentUser()!!.displayName}" + "CURENTUSER ==> ${currentUser.username}"
                    )
                    // this.profile_activity_check_box_is_mentor.isChecked = currentUser.isMentor!!
                    textInputEditTextUsername!!.setText(username)
                }
        }
    }

    private fun updateUI() {
        var email: String? = null
        if (this.getCurrentUser() != null) {
            email =
                if (TextUtils.isEmpty(this.getCurrentUser()!!.email)) getString(R.string.info_no_email_found) else this.getCurrentUser()!!.email
            this.profile_activity_text_view_email.text = email
            val username =
                if (TextUtils.isEmpty(getCurrentUser()!!.displayName)) getString(R.string.info_no_username_found) else getCurrentUser()!!.displayName
            firestoreUser.addSnapshotListener { documentSnapshot, e ->
                when {
                    e != null -> Log.e("ERROR", e.message)
                    documentSnapshot != null
                            && documentSnapshot.exists() -> {
                        textInputEditTextUsername!!.setText(username)
                    }
                }
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
            if (username.isNotEmpty() && !username.equals(getString(R.string.info_no_username_found))) {
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