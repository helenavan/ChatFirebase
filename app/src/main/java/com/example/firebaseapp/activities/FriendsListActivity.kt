package com.example.firebaseapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseapp.R
import com.example.firebaseapp.api.addUsersListener
import com.example.firebaseapp.recyclerview.FriendItem
import com.example.firebaseapp.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_friends_list.*

class FriendsListActivity : AppCompatActivity() {

    private lateinit var userListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true

    private lateinit var peopleSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        userListenerRegistration =
            addUsersListener(applicationContext, this::updateRecyclerView)
    }

    private fun updateRecyclerView(items: List<Item>) {

        fun init() {
            recycler_view_people.apply {
                layoutManager = LinearLayoutManager(this@FriendsListActivity.applicationContext)
                adapter = GroupAdapter<ViewHolder>().apply {
                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = peopleSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is FriendItem) {
            Toast.makeText(applicationContext,"id chat :  ${item.userId}", Toast.LENGTH_SHORT).show()
/*            val intent = Intent(applicationContext,ChatActivity::class.java).apply {
                putExtra( USER_ID , item.userId)
            }
            startActivity(intent)*/
/*            startActivity<ChatActivity>(
                USER_NAME to item.person.name,
                AppConstants.USER_ID to item.userId
            )*/
        }
    }
}
