package com.example.firebaseapp.auth

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.MentorChatActivity
import com.example.firebaseapp.R
import com.example.firebaseapp.api.*
import com.example.firebaseapp.base.BaseActivity
import com.example.firebaseapp.getListUID

import com.example.firebaseapp.models.Friend
import com.example.firebaseapp.models.Friends
import com.example.firebaseapp.models.Room
import com.example.firebaseapp.models.User
import com.example.firebaseapp.views.HintSpinnerAdapter
import com.example.firebaseapp.views.showSnackBar
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.main_activity_button_chat
import kotlinx.android.synthetic.main.activity_profile.view.*


private const val SIGN_OUT_TASK: Int = 10
private const val DELETE_USER_TASK = 20
private const val UPDATE_USERNAME: Int = 30
private const val NEW_SPINNER_ID = 1

class ProfileActivity : BaseActivity(),AdapterView.OnItemSelectedListener {

    private var textInputEditTextUsername: EditText? = null
    private var imageProfil: ImageView? = null
    private var linearLayout: ConstraintLayout? = null
    private var nameRoom:String? = null
    private var list_rooms = mutableListOf<String>("room1","room2","room3")
    private var spinner:Spinner? = null
    private val firestoreUser by lazy {
        FirebaseFirestore.getInstance().collection("users").document(getCurrentUser()!!.uid)
    }

    override val fragmentLayout: Int
        get() = R.layout.activity_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textInputEditTextUsername = findViewById(R.id.profile_activity_edit_text_username)
        imageProfil = findViewById(R.id.profile_activity_imageview_profile)
        linearLayout = findViewById(R.id.profile_container)
        spinner = findViewById(R.id.profile_spinner)
        spinner!!.onItemSelectedListener = this
        this.selectItemSpinner()
        this.onClickDeleButton()
        this.onClickSignOutButton()
        this.onClickUpdateButton()
        this.onClickChatButton()
        this.updateUIWhenCreating()
       // this.createFriendsList()
    }

    private fun onClickUpdateButton() {
        profile_activity_button_update.setOnClickListener {
            this.updateUsernameInFirebase()
            this.createFriendsList()
        }
    }

    private fun onClickChatButton() {
        this.main_activity_button_chat.setOnClickListener {
            if (this.isCurrentUserLogged()) {
                this.startMentorChatActicity()
            } else {
                // showSnackBar(this.linearLayout!!, getString(R.string.error_not_connected))
            }
        }
    }

    //create an array adapter for spinner
    private fun selectItemSpinner(){
        var arrayRooms = HintSpinnerAdapter(this, android.R.layout.simple_spinner_item)
        arrayRooms.addAll(list_rooms)
       // arrayRooms.add("Choisissez votre room")
        arrayRooms.add(getString(R.string.title_spinner))
        arrayRooms.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        with(spinner){
            this!!.adapter = arrayRooms
            //setSelection(0,false)
            setSelection(adapter.count)
            onItemSelectedListener = this@ProfileActivity
            prompt = getString(R.string.title_spinner)
            gravity = Gravity.CENTER
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(spinner!!.selectedItem == getString(R.string.title_spinner)){}
        else{
           // room = Room(nameRoom = list_rooms[position])
            nameRoom = list_rooms[position]
            Log.e("ProfileActivity", "chat selected => $nameRoom")
            Toast.makeText(this,"item selected position: "+ list_rooms[position],Toast.LENGTH_LONG).show()
        }

    }

    private fun startMentorChatActicity() {
        if(nameRoom != null){
            val intent: Intent = Intent(applicationContext, MentorChatActivity::class.java)
            intent.putExtra("room", nameRoom)
            startActivity(intent)
        }else{
            Toast.makeText(this,"Vous n'avez pas choisi de salon",Toast.LENGTH_SHORT).show()
        }
    }

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
          /*  Log.e(
                "ProfilActivity",
                "UpdateUIWhenCreating photo ===> ${getCurrentUser()!!.photoUrl}"
            )*/
            //Get picture URL from Firebase

            //Get email & username from Firebase
            email =
                if (TextUtils.isEmpty(this.getCurrentUser()!!.email)) getString(R.string.info_no_email_found) else this.getCurrentUser()!!.email

            //Update com.example.firebaseapp.views with data
            this.profile_activity_text_view_email.text = email

            // 5 - Get additional data from Firestore
            getUser(this.getCurrentUser()!!.uid)
                .addOnSuccessListener { documentSnapshot ->
                    val currentFriend = documentSnapshot.toObject(Friend::class.java)
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
                    val username =
                        if (TextUtils.isEmpty(currentUser!!.username)) getString(R.string.info_no_username_found) else currentUser.username
                    textInputEditTextUsername!!.setText(username)
                }
        }
    }

    //update en temps réel
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
                changeNameAuth(username)
            }
        }
    }

    private fun changeNameAuth(username: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        this.getCurrentUser()!!.updateProfile(profileUpdates)
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

    //Create a list of friends
    private fun createFriendsList(){
       val listF= getListUID("users",
            firestoreUser.id)
        Log.d("ProfileActivity", "my friends : $listF")
        //FriendHelper.creatFriend(firestoreUser.id, )
      //  updateListFriends(firestoreUser.id,listF)
    }
}