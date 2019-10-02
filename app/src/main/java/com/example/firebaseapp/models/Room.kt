package com.example.firebaseapp.models

data class Room(val userIds: MutableList<String>){
    constructor(): this(mutableListOf())
}

