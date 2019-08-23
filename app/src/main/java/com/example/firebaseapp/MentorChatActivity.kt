package com.example.firebaseapp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.api.MessageHelper
import com.example.firebaseapp.api.UserHelper
import com.example.firebaseapp.base.BaseActivity
import com.example.firebaseapp.models.User
import com.example.firebaseapp.models.Message
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_mentor_chat.*
import pub.devrel.easypermissions.EasyPermissions

import java.util.UUID

class MentorChatActivity : BaseActivity(), MentorChatAdapter.Listener {

    // FOR DATA
    private var mentorChatAdapter: MentorChatAdapter? = null
    private var modelCurrentUser: User? = null
    private var currentChatName: String? = null
    private var uriImageSelected: Uri? = null

    private var editTextMessage:EditText? = null
    private var textViewRecyclerViewEmpty:TextView?= null
    private var imageViewPreview:ImageView? = null
    private var recyclerView:RecyclerView? = null

    override val fragmentLayout: Int
        get() = R.layout.activity_mentor_chat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = findViewById(R.id.activity_mentor_chat_recycler_view)
        textViewRecyclerViewEmpty = findViewById(R.id.activity_mentor_chat_text_view_recycler_view_empty)
        imageViewPreview = findViewById(R.id.activity_mentor_chat_image_chosen_preview)
        editTextMessage = findViewById(R.id.activity_mentor_chat_message_edit_text)
        this.configureRecyclerView(CHAT_NAME_ANDROID)
        this.configureToolbar()
        this.getCurrentUserFromFirestore()

        activity_mentor_chat_send_button.setOnClickListener{
            this.onClickSendMessage()
        }
        this.onClickChatButtons()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponse(requestCode, resultCode, data)
    }

    // --------------------
    // ACTIONS
    // --------------------


    private fun onClickSendMessage() {
        if (!TextUtils.isEmpty(editTextMessage!!.text) && modelCurrentUser != null) {
            // Check if the ImageView is set
            if (this.imageViewPreview!!.getDrawable() == null) {
                // SEND A TEXT MESSAGE
                MessageHelper.createMessageForChat(
                    editTextMessage!!.text!!.toString(),
                    this.currentChatName!!,
                    modelCurrentUser!!
                ).addOnFailureListener(this.onFailureListener())
                this.editTextMessage!!.setText("")
            } else {
                // SEND A IMAGE + TEXT IMAGE
                this.uploadPhotoInFirebaseAndSendMessage(editTextMessage!!.text!!.toString())
                this.editTextMessage!!.setText("")
                this.imageViewPreview!!.setImageDrawable(null)
            }
        }
    }

    private fun onClickChatButtons() {
        activity_mentor_chat_android_chat_button.setOnClickListener{
            this.configureRecyclerView(CHAT_NAME_ANDROID)
        }
        activity_mentor_chat_firebase_chat_button.setOnClickListener{
            this.configureRecyclerView(CHAT_NAME_FIREBASE)
        }
        activity_mentor_chat_bug_chat_button.setOnClickListener{
            this.configureRecyclerView(CHAT_NAME_BUG)
            Toast.makeText(this,"message $CHAT_NAME_BUG",Toast.LENGTH_LONG).show()
        }
    }

    private fun onClickAddFile() {
        this.chooseImageFromPhone()
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private fun getCurrentUserFromFirestore() {
        UserHelper.getUser(getCurrentUser()!!.uid).addOnSuccessListener { documentSnapshot ->
            modelCurrentUser = documentSnapshot.toObject<User>(User::class.java)
        }
    }

    private fun uploadPhotoInFirebaseAndSendMessage(message: String) {
        val uuid = UUID.randomUUID().toString() // GENERATE UNIQUE STRING
        // A - UPLOAD TO GCS
        val mImageRef = FirebaseStorage.getInstance().getReference(uuid)
        mImageRef.putFile(this.uriImageSelected!!)
            .addOnSuccessListener(
                this
            ) { taskSnapshot ->
                val pathImageSavedInFirebase = taskSnapshot.metadata!!.toString()
                // B - SAVE MESSAGE IN FIRESTORE
                MessageHelper.createMessageWithImageForChat(
                    pathImageSavedInFirebase,
                    message,
                    currentChatName!!,
                    modelCurrentUser!!
                ).addOnFailureListener(onFailureListener())
            }
            .addOnFailureListener(this.onFailureListener())
    }

    // --------------------
    // FILE MANAGEMENT
    // --------------------

    private fun chooseImageFromPhone() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.popup_title_permission_files_access),
                RC_IMAGE_PERMS,
                PERMS
            )
            return
        }
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RC_CHOOSE_PHOTO)
    }

    private fun handleResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) { //SUCCESS
                this.uriImageSelected = data!!.data
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                    .load(this.uriImageSelected)
                    .apply(RequestOptions.circleCropTransform())
                    .into(this.imageViewPreview!!)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.toast_title_no_image_chosen),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --------------------
    // UI
    // --------------------

    private fun configureRecyclerView(chatName: String) {
        //Track current chat name
        this.currentChatName = chatName
        //Configure Adapter & RecyclerView
        this.mentorChatAdapter = MentorChatAdapter(
            generateOptionsForAdapter(MessageHelper.getAllMessageForChat(this.currentChatName!!)),
            Glide.with(this),
            this,
            this.getCurrentUser()!!.uid
        )
        mentorChatAdapter!!.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerView!!.smoothScrollToPosition(mentorChatAdapter!!.itemCount) // Scroll to bottom on new messages
            }
        })
        recyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView!!.adapter = this.mentorChatAdapter
    }

    private fun generateOptionsForAdapter(query: Query): FirestoreRecyclerOptions<Message> {
        return FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .setLifecycleOwner(this)
            .build()
    }

    // --------------------
    // CALLBACK
    // --------------------

    override fun onDataChanged() {
        textViewRecyclerViewEmpty!!.visibility = if (this.mentorChatAdapter!!.itemCount == 0) View.VISIBLE else View.GONE
    }

    companion object {

        // STATIC DATA FOR CHAT
        private val CHAT_NAME_ANDROID = "android"
        private val CHAT_NAME_BUG = "bug"
        private val CHAT_NAME_FIREBASE = "firebase"

        // STATIC DATA FOR PICTURE
        private val PERMS = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val RC_IMAGE_PERMS = 100
        private val RC_CHOOSE_PHOTO = 200
    }
}