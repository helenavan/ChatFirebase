package com.example.firebaseapp.views

import android.graphics.Color
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.firebaseapp.R
import com.google.android.material.snackbar.Snackbar

//show Snack Bar with a message
fun showSnackBar(coordinatorLayout: ConstraintLayout, message: String) {
    val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundColor(Color.LTGRAY)
    val textView =
        snackbarView.findViewById(R.id.snackbar_text) as TextView
    textView.setTextColor(Color.BLACK)
    snackbar.show()
}