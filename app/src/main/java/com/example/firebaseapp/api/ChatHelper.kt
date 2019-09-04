package com.example.firebaseapp.api

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference



private const val COLLECTION_NAME:String = "rooms"

class ChatHelper {

    companion object{
        fun getChatCollection(): CollectionReference {
            return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
        }
    }
}