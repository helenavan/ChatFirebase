package com.example.firebaseapp

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bumptech.glide.RequestManager
import com.example.firebaseapp.models.Message
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class MentorChatAdapter(
    options: FirestoreRecyclerOptions<Message>, //FOR DATA
    private val callback: Listener, private val idCurrentUser: String
) : FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {

    interface Listener {
        fun onDataChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        holder.updateWithMessage(model, this.idCurrentUser)
        Log.e("ADAPTER", "set position message sent ====> ${model.message} $position")
    }

/*    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(inflater, parent)
    }*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_mentor_chat_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        this.callback.onDataChanged()
    }
}