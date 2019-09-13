package com.example.firebaseapp.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LikeService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}