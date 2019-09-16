package com.example.firebaseapp.models

import com.google.firebase.firestore.core.OnlineState

data class User(var uid:String? = null,
                var username:String? = null,
                var urlPicture:String? = null,
                var friends:List<Friend>? = null)



