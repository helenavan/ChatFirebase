package com.example.firebaseapp.models

import com.google.firebase.firestore.core.OnlineState

data class User(var username:String? = null,
                var bio:String,
                var urlPicture:String? = null,
                val registrationTokens: MutableList<String>){
    constructor(): this("", "", "null", mutableListOf())
}





