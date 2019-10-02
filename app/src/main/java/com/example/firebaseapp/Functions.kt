package com.example.firebaseapp

import android.util.Log
import com.example.firebaseapp.models.Friend
import com.example.firebaseapp.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

fun getListUID(rootCollection: String, userID: String, isOnline:Boolean):List<User>{

    var user: User? = null
    val list = mutableListOf<User>()
    val db = FirebaseFirestore.getInstance()
    var nameFriend:String? = null
    db.collection(rootCollection).get()
        .addOnCompleteListener{ task ->
            if (task.isSuccessful) {

                for (document in task.result!!) {
                    //TODO booleans
                   // friend = Friend(idFriends = document.id, connected = isonline)
                     nameFriend = document.get("username") as String
                    user = User(bio = document.id,username = nameFriend, registrationTokens = mutableListOf())
                    if (document.id != userID) {
                         list.add(user!!)
                   //  creatFriend(document.id,userID, online = isonline)
                      //  Log.d("Function_connected", "Value :  $truc")
                    }
                }
              //  updateFriends(userID,friend!!)
                Log.d("Function_getUID", user.toString() + " current userID :  $userID")
            } else {
                Log.e("getUID", "Error getting documents: ", task.exception)
            }
        }
    return list
}

/*
//to retrieve string isonline
getFriendOnline(document.id, object: MyCallback{
    override fun onCallback(value: String) {
        connected = value

        Log.d("Function_connected", "Value :  $connected")
    }
})*/
