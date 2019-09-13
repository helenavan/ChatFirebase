package com.example.firebaseapp.models

data class Message(var message:String? = null,
                   var dateCreated:String? = null,
                   var userSender:User? = null,
                   var urlImage:String? = null,
                   var viewvers:List<Friend>? = null)
