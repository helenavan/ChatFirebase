package com.example.firebaseapp

import android.util.Log
import com.example.firebaseapp.api.updateListFriends
import com.example.firebaseapp.models.Friend
import com.example.firebaseapp.models.Friends
import com.example.firebaseapp.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

fun getListUID(rootCollection: String, userID: String): List<Friend>{

    var friend:Friend
    val list = mutableListOf<Friend>()
    val db = FirebaseFirestore.getInstance()
    db.collection(rootCollection).get()
        .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
            if (task.isSuccessful) {

                for (document in task.result!!) {
                    //TODO booleans
                    friend = Friend(idFriends = document.id, isConnected = true,isFriend =  true)
                    if (document.id != userID) {
                        list.add(friend)
                    }
                }
                updateListFriends(userID,list)
                Log.d("Function_getUID", list.toString() + "userID :  $userID")
            } else {
                Log.e("getUID", "Error getting documents: ", task.exception)
            }
        })
    return list
}