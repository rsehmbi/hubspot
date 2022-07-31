package com.example.hubspot.schedule

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.schedule.CourseListViewModel.CourseOutlineViewModel
import com.example.hubspot.schedule.CourseListViewModel.CourseOutlineViewModelFactory
import com.example.hubspot.schedule.Repository.repository

class SingleCourseActivity : AppCompatActivity() {
    private lateinit var courseOutlineViewModel: CourseOutlineViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_course)

        val extras = intent.extras
        val courseID: String?

        if (extras != null) {
            courseID = extras.getString("COURSE_ID")
            var textView = findViewById<TextView>(R.id.tempViewTextId)
            textView.text = "You clicked on ${courseID}. This feature is under development"

            val repository = repository()
            val viewModelFactory = CourseOutlineViewModelFactory(repository)
            courseOutlineViewModel = ViewModelProvider(this, viewModelFactory).get(CourseOutlineViewModel::class.java)

            if (!courseID.isNullOrEmpty()){
                val courseNumber = courseID.split(" ").get(1)
                courseOutlineViewModel.getCourseOutline("course-outlines?current/current/cmpt/${courseNumber}/d100")
                courseOutlineViewModel.myOutlineReponse.observe(this) { response ->
                    if (response.isSuccessful) {
                        println("raman debug" + (response.body()?.courseSchedule?.get(0)?.campus))
                    }
                }
            }
        }



    }
}