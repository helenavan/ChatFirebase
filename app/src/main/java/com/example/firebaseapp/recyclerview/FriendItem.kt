package com.example.firebaseapp.recyclerview

import android.content.Context
import com.example.firebaseapp.R
import com.example.firebaseapp.base.GlideApp
import com.example.firebaseapp.models.User
import com.example.firebaseapp.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_friend.*

class FriendItem(val friend: User,
                 val userId: String,
                 val context: Context) :Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = friend.username
        viewHolder.textView_bio.text = friend.bio
        if (friend.urlPicture != null)
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(friend.urlPicture!!))
                .placeholder(R.drawable.ic_moustache480px)
                .into(viewHolder.imageView_profile_picture)
    }

    override fun getLayout() = R.layout.item_friend
}