package com.example.firebaseapp.api

import com.example.firebaseapp.api.ListFriendsHelper.Companion.getFriendsCollection
import com.example.firebaseapp.models.Friend
import com.example.firebaseapp.models.Friends
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

private const val COLLECTION_NAME:String = "friendList"

class FriendHelper {
    companion object{

        fun creatFriend(uid:String,idFriend:String):Task<DocumentReference>{
            val friendUser = Friend(idFriend,true,true)
            return getFriendsCollection().document(uid).collection(COLLECTION_NAME).add(friendUser)
        }

    }
}