package com.example.firebaseapp.api

import com.example.firebaseapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

private  const val COLLECTION_NAME:String = "users"

fun getUsersCollection(): CollectionReference {
    return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
}

// --- CREATE ---

fun createUser(uid: String, username: String, urlPicture: String): Task<Void> {
    val userToCreate = User(uid = uid, username = username, urlPicture = urlPicture)
    return getUsersCollection().document(uid).set(userToCreate)
}

// --- GET ---

fun getUser(uid: String): Task<DocumentSnapshot> {
    return getUsersCollection().document(uid).get()
}

// --- UPDATE ---

fun updateUsername(username: String, uid: String): Task<Void> {
    return getUsersCollection().document(uid).update("username", username)
}

fun updateIsMentor(uid: String, isMentor: Boolean?): Task<Void> {
    return getUsersCollection().document(uid).update("isMentor", isMentor)
}

// --- DELETE ---

fun deleteUser(uid: String): Task<Void> {
    return getUsersCollection().document(uid).delete()
}