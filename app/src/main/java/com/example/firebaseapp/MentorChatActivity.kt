package com.example.firebaseapp

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.api.MessageHelper
import com.example.firebaseapp.api.getUser
import com.example.firebaseapp.base.BaseActivity
import com.example.firebaseapp.models.User
import com.example.firebaseapp.models.Message
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_mentor_chat.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MentorChatActivity : BaseActivity(), MentorChatAdapter.Listener {

    // FOR DATA
    private var mentorChatAdapter: MentorChatAdapter? = null
    private var modelCurrentUser: User? = null
    private var currentChatName: String? = null
    private var uriImageSelected: Uri? = null
    private var dateChat: String? = null
    private var modelMessage: Message? = null

    private var editTextMessage:EditText? = null
    private var textViewRecyclerViewEmpty:TextView?= null
    private var imageViewPreview:ImageView? = null
    private var recyclerView:RecyclerView? = null
    private var pathImageSavedInFirebase:String?= null

    override val fragmentLayout: Int
        get() = R.layout.activity_mentor_chat


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = findViewById(R.id.activity_mentor_chat_recycler_view)
        textViewRecyclerViewEmpty = findViewById(R.id.activity_mentor_chat_text_view_recycler_view_empty)
        imageViewPreview = findViewById(R.id.activity_mentor_chat_image_chosen_preview)
        editTextMessage = findViewById(R.id.activity_mentor_chat_message_edit_text)
        dateChat = convertDateToHour()
        this.configureRecyclerView(CHAT_NAME_ANDROID)
        this.configureToolbar()
        this.getCurrentUserFromFirestore()

        activity_mentor_chat_send_button.setOnClickListener{
            this.onClickSendMessage()
        }
        this.onClickChatButtons()
        this.onClickAddFile()
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
            if (this.imageViewPreview!!.drawable == null) {
                // SEND A TEXT MESSAGE
                MessageHelper.createMessageForChat(
                    editTextMessage!!.text!!.toString(),
                    this.currentChatName!!,
                    modelCurrentUser!!,
                    dateChat!!

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

    @AfterPermissionGranted(RC_IMAGE_PERMS)
    private fun onClickAddFile() {
        activity_mentor_chat_add_file_button.setOnClickListener { this.chooseImageFromPhone() }

    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private fun getCurrentUserFromFirestore() {
        getUser(getCurrentUser()!!.uid).addOnSuccessListener { documentSnapshot ->
            modelCurrentUser = documentSnapshot.toObject<User>(User::class.java)
        }
    }

    //upload a picture in Firebase ans send a message
    private fun uploadPhotoInFirebaseAndSendMessage(message: String) {
        val uuid = UUID.randomUUID().toString() // GENERATE UNIQUE STRING
        // A - UPLOAD TO GCS and compress
        val mImageRef = FirebaseStorage.getInstance().getReference(uuid)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uriImageSelected)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,stream)
        val byteArray = stream.toByteArray()
        mImageRef.putBytes(byteArray)
            .addOnSuccessListener(
                this
            ) { taskSnapshot ->
                pathImageSavedInFirebase = taskSnapshot.metadata!!.toString()
                Log.e("MentorAct", "path image from storage : $pathImageSavedInFirebase")
                // B - SAVE MESSAGE IN FIRESTORE
                MessageHelper.createMessageWithImageForChat(
                    pathImageSavedInFirebase!!,
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
               val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uriImageSelected)
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                    //.load(this.uriImageSelected)
                    .load(this.compressBitmap(bitmap,20))
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
            this,
            this.getCurrentUser()!!.uid
        )
        mentorChatAdapter!!.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerView!!.smoothScrollToPosition(mentorChatAdapter!!.itemCount) // Scroll to bottom on new messages
            }
        })
        recyclerView!!.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL, true)
        recyclerView!!.adapter = this.mentorChatAdapter
        recyclerView!!.adapter!!.notifyDataSetChanged()
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

    private fun compressBitmap(bitmap: Bitmap, quality:Int):Bitmap{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream)
        val byteArray = stream.toByteArray()

        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDateToHour(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return current.format(formatter)
    }
}