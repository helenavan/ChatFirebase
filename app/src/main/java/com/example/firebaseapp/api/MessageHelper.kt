package com.example.firebaseapp.api

import android.content.Context
import android.util.Log
import com.example.firebaseapp.api.ChatHelper.Companion.getChatCollection
import com.example.firebaseapp.models.Message
import com.example.firebaseapp.models.User
import com.example.firebaseapp.recyclerview.FriendItem
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item

private const val COLLECTION_NAME = "messages"

class MessageHelper {
    companion object {
        //query pour affiner un e recherche à travers une collection
        fun getAllMessageForChat(chat: String): Query {
            return getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50)
        }

        fun createMessageForChat(
            textMessage: String,
            chat: String,
            userSender: User,
            dateMessage: String
        ): Task<DocumentReference> {
            //create the Message object
            val message = Message(textMessage, userSender = userSender, dateCreated = dateMessage)

            //store Message to Firestore
            return getChatCollection().document(chat).collection(COLLECTION_NAME)
                .add(message)//add() permet d'ajouter un id unique au message
        }

        fun createMessageWithImageForChat(
            urlImage: String,
            textMessage: String,
            chat: String,
            userSender: User,
            dateMessage: String
        ): Task<DocumentReference> {
            val message = Message(
                textMessage,
                urlImage = urlImage,
                userSender = userSender,
                dateCreated = dateMessage
            )
            return ChatHelper.getChatCollection().document(chat).collection(COLLECTION_NAME)
                .add(message)
        }
    }

    fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return getUsersCollection()
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(FriendItem(it.toObject(User::class.java)!!, it.id, context))
                }
                onListen(items)
            }
    }
}