package com.example.firebaseapp.models

import java.util.*

data class Message(var message:String? = null,
                   var dateCreated:Date? = null,
                   var userSender:User? = null,
                   var urlImage:String? = null) {
}