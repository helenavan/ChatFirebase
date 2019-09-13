package com.example.firebaseapp.api

import com.example.firebaseapp.api.ChatHelper.Companion.getChatCollection
import com.example.firebaseapp.models.Message
import com.example.firebaseapp.models.User
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.Task
import java.util.*


private const val COLLECTION_NAME = "messages"

class MessageHelper {
    companion object{
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
            dateMessage:String
        ): Task<DocumentReference> {
            //create the Message object
            val message = Message(textMessage,userSender =  userSender, dateCreated =dateMessage )

            //store Message to Firestore
            return getChatCollection().document(chat).collection(COLLECTION_NAME)
                .add(message)//add() permet de d'ajouter un id unique au message
        }

        fun createMessageWithImageForChat(
            urlImage: String,
            textMessage: String,
            chat: String,
            userSender: User,
            dateMessage: String
        ): Task<DocumentReference> {
            val message = Message(textMessage,urlImage =  urlImage, userSender = userSender, dateCreated = dateMessage)
            return ChatHelper.getChatCollection().document(chat).collection(COLLECTION_NAME)
                .add(message)
        }
    }
}