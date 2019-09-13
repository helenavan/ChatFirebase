package com.example.firebaseapp.api

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

private const val COLLECTION_NAME:String = "friends"

class ListFriendsHelper {

    companion object{
        fun getFriendsCollection(): CollectionReference {
            return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
        }
    }
}