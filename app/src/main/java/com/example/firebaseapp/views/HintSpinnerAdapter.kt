package com.example.firebaseapp.views

import android.content.Context
import android.widget.ArrayAdapter

class HintSpinnerAdapter(context: Context,textViewRessource:Int ): ArrayAdapter<String>(context,textViewRessource) {

    override fun getCount(): Int {
       val count:Int = super.getCount()
        return if (count > 0) count - 1 else count
        }
}