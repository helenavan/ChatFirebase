package com.example.firebaseapp.models

data class User(var uid:String? = null,
                var username:String? = null,
                var isMentor:Boolean = false,
                var urlPicture:String? = null)

