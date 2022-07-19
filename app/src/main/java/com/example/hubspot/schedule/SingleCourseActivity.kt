package com.example.hubspot.schedule

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.R

class SingleCourseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_course)

        val extras = intent.extras
        val courseID: String?

        if (extras != null) {
            courseID = extras.getString("COURSE_ID")
            var textView = findViewById<TextView>(R.id.tempViewTextId)
            textView.text = "You clicked on ${courseID}. This feature is under development"
        }
    }
}