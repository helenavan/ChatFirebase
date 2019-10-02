package com.example.firebaseapp.api

import android.content.Context
import android.util.Log
import com.example.firebaseapp.models.Friend
import com.example.firebaseapp.models.User
import com.example.firebaseapp.recyclerview.FriendItem
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.xwray.groupie.kotlinandroidextensions.Item

private  const val COLLECTION_NAME:String = "users"

private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

private val currentUserDocRef: DocumentReference
    get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
        ?: throw NullPointerException("UID is null.")}")

fun getUsersCollection(): CollectionReference {
    return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
}

// --- CREATE ---

fun createUser(
    uid: String,
    username: String,
    urlPicture: String,
    addTokenToFirestore: Unit
): Task<Void> {
    val userToCreate = User(bio = uid, username = username, urlPicture = urlPicture,registrationTokens = mutableListOf())
    return getUsersCollection().document(uid).set(userToCreate)
}

fun subcreateFriend(uid:String,friend:Friend): Task<DocumentReference> {
    val user = getUsersCollection().document(uid).collection("friends")

   // val friend = Friend(idFriends = idF,connected =  online)

    return user.add(friend)
}
// --- GET ---

fun getUser(uid: String): Task<DocumentSnapshot> {
    return getUsersCollection().document(uid).get()
}

fun getFriend(uid:String): Task<DocumentSnapshot> {
    return getUsersCollection().document(uid).collection("friends").document().get()
}

// --- UPDATE ---

fun updateUsername(username: String, uid: String): Task<Void> {
    return getUsersCollection().document(uid).update("username", username)
}

/*fun updateListFriends(uid:String,listF:List<Friend>):Task<Void>{
    return getUsersCollection().document(uid).update("friends",listF)
}*/
fun updateFriends(uid:String,friend: Friend):Task<Void>{
    return getUsersCollection().document(uid).update("friend",friend)
}
// --- DELETE ---

fun deleteUser(uid: String): Task<Void> {
    return getUsersCollection().document(uid).delete()
}


//--add in list of friends ---
fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
    return getUsersCollection()
        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            val items = mutableListOf<Item>()
            querySnapshot!!.documents.forEach {
                if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                    items.add(FriendItem(it.toObject(User::class.java)!!, it.id, context))
            }
            onListen(items)
        }
}


fun makeUserOnline(uid: String): Task<Void> {
    //Firestore status
    var query = getUsersCollection().document(uid!!)
   // var fbquery =FirebaseDatabase.getInstance().getReference("status/" + uid).setValue("online")

   // FirebaseDatabase.getInstance().getReference("/status/" + uid).onDisconnect().setValue("offline")

    return query.update(
        "isOnline", true,
        "status", "online"
    )
}

fun makeUserOffline(uid: String): Task<Void> {
    val query = getUsersCollection().document(uid!!)
  //  var fbquery = FirebaseDatabase.getInstance().getReference("status/$uid").setValue("offline")

    return query.update("isOnline",false,
        "status", "offline",
        "last_active", System.currentTimeMillis()
    )
}

fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
    currentUserDocRef.get().addOnSuccessListener {
        val user = it.toObject(User::class.java)!!
        onComplete(user.registrationTokens)
    }
}

fun getFCMRegistrationTokens(){

}

fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
    currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
}
