package com.example.firebaseapp

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.example.firebaseapp.models.Message
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class MentorChatAdapter(
    options: FirestoreRecyclerOptions<Message>, //FOR DATA
    private val glide: RequestManager, //FOR COMMUNICATION
    private val callback: Listener, private val idCurrentUser: String
) : FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {

    interface Listener {
        fun onDataChanged()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        holder.updateWithMessage(model, this.idCurrentUser, this.glide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(inflater, parent)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        this.callback.onDataChanged()
    }
}