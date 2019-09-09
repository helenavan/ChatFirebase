package com.example.firebaseapp

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseapp.R.drawable.chat_receiver
import com.example.firebaseapp.base.GlideApp
import com.example.firebaseapp.models.Message
import com.google.firebase.storage.FirebaseStorage


class MessageViewHolder(item: View) :
    RecyclerView.ViewHolder(item) {

/*    private var colorCurrentUser: Int = 0
    private var colorRemoteUser: Int = 0*/
    private var colorCurrentUser:Drawable?=null
    private var colorRemoteUser:Drawable?=null
    private var rootView: RelativeLayout? = null
    private var profileContainer: LinearLayout? = null
    private var imageViewProfile: ImageView? = null
    private var imageViewIsMentor: ImageView? = null
    private var messageContainer: RelativeLayout? = null
    private var cardViewImageSent: CardView? = null
    private var imageViewSent: ImageView
    private var textMessageContainer: LinearLayout? = null
    private var textViewMessage: TextView? = null
    private var textViewDate: TextView? = null

    init {
       // colorCurrentUser = ContextCompat.getColor(itemView.context, R.color.colorAccent)
      //  colorRemoteUser = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
        colorCurrentUser = ContextCompat.getDrawable(itemView.context, R.drawable.chat_sender)
        colorRemoteUser = ContextCompat.getDrawable(itemView.context, chat_receiver)

        rootView = itemView.findViewById(R.id.activity_mentor_chat_item_root_view)
        profileContainer = itemView.findViewById(R.id.activity_mentor_chat_item_profile_container)
        this.imageViewProfile =
            itemView.findViewById(R.id.activity_mentor_chat_item_profile_container_profile_image)
        messageContainer = itemView.findViewById(R.id.activity_mentor_chat_item_message_container)
        this.cardViewImageSent =
            itemView.findViewById(R.id.activity_mentor_chat_item_message_container_image_sent_cardview)
        this.imageViewSent =
            itemView.findViewById(R.id.activity_mentor_chat_item_message_container_image_sent_cardview_image)
        textMessageContainer =
            itemView.findViewById(R.id.activity_mentor_chat_item_message_container_text_message_container)
        textViewMessage =
            itemView.findViewById(R.id.activity_mentor_chat_item_message_container_text_message_container_text_view)
        textViewDate =
            itemView.findViewById(R.id.activity_mentor_chat_item_message_container_text_view_date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateWithMessage(message: Message, currentUserId: String) {

        // Check if current user is the sender
        val isCurrentUser = message.userSender!!.uid.equals(currentUserId)
        val storageReference = FirebaseStorage.getInstance().reference
        // Update message TextView
        this.textViewMessage!!.text = message.message
        this.textViewMessage!!.textAlignment =
            if (isCurrentUser) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START

        // Update date TextView
        if (message.dateCreated != null)
            textViewDate!!.text = message.dateCreated

        // Update isMentor ImageView
/*        this.imageViewIsMentor!!.visibility =
            if (message.userSender!!.isMentor) View.VISIBLE else View.INVISIBLE*/

        // Update profile picture ImageView
        if (message.userSender!!.urlPicture != null){
            Glide.with(itemView)
                .load(message.userSender!!.urlPicture)
                .placeholder(R.drawable.ic_moustache480px)
                .apply(RequestOptions.circleCropTransform())
                .into(imageViewProfile!!)
        }else{ imageViewProfile!!.setImageResource(R.drawable.ic_moustache480px)}
        Log.e("HOLDER", "Photo profil ==> ${message.userSender!!.urlPicture}")

        // Update image sent ImageView
        if (message.urlImage != null) {
            GlideApp.with(itemView.context)
                .load(storageReference.child(message.urlImage.toString()))
                .placeholder(R.drawable.ic_digital_library)
                .into(imageViewSent)

            this.imageViewSent!!.visibility = View.VISIBLE
        } else {
            this.imageViewSent!!.visibility = View.GONE
        }

        //Update Message Bubble Color Background
       // (textMessageContainer!!.background as GradientDrawable).setColor(if (isCurrentUser) colorCurrentUser else colorRemoteUser)
        messageContainer!!.background = if( isCurrentUser) colorCurrentUser else colorRemoteUser
        // Update all com.example.firebaseapp.views alignment depending is current user or not
        this.updateDesignDependingUser(isCurrentUser)
    }

    private fun updateDesignDependingUser(isSender: Boolean) {
        //PROFILE CONTAINER
        val paramsLayoutHeader = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        paramsLayoutHeader.addRule(if (isSender) RelativeLayout.ALIGN_PARENT_RIGHT else RelativeLayout.ALIGN_PARENT_LEFT)
        this.profileContainer!!.layoutParams = paramsLayoutHeader

        //MESSAGE CONTAINER
        val paramsLayoutContent = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        paramsLayoutContent.addRule(
            if (isSender) RelativeLayout.LEFT_OF else RelativeLayout.RIGHT_OF,
            R.id.activity_mentor_chat_item_profile_container
        )
        this.messageContainer!!.layoutParams = paramsLayoutContent

        //CARDVIEW IMAGE SEND
        val paramsImageView = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        paramsImageView.addRule(
            if (isSender) RelativeLayout.ALIGN_LEFT else RelativeLayout.ALIGN_RIGHT,
            R.id.activity_mentor_chat_item_message_container_text_message_container
        )

        this.cardViewImageSent!!.layoutParams= paramsImageView
        Log.e("HOLDER", "paramsImageView ==> ${paramsImageView.toString()}")

        this.rootView!!.requestLayout()
    }
}