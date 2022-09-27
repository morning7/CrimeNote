package com.android.study

import android.view.View
import androidx.appcompat.app.AppCompatActivity

class TestActivity: AppCompatActivity() {

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setContentView(R.layout.test)
    }
}