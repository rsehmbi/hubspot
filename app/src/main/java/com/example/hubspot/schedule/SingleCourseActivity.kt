package com.example.hubspot.schedule

import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.NetworkUtil
import com.example.hubspot.R
import com.example.hubspot.schedule.CourseListViewModel.CourseOutlineViewModel
import com.example.hubspot.schedule.CourseListViewModel.CourseOutlineViewModelFactory
import com.example.hubspot.schedule.Models.CourseOutline
import com.example.hubspot.schedule.Models.CourseSchedule
import com.example.hubspot.schedule.Models.Info
import com.example.hubspot.schedule.Models.Instructor
import com.example.hubspot.schedule.Repository.repository

class SingleCourseActivity : AppCompatActivity() {
    private lateinit var courseOutlineViewModel: CourseOutlineViewModel

    lateinit var singlecourseNameId:TextView;
    lateinit var titleId:TextView;
    lateinit var termId:TextView;
    lateinit var prereqId:TextView;
    lateinit var unitsId:TextView;
    lateinit var registrationNotesId:TextView;
    lateinit var courseDetailsId:TextView;

    lateinit var roomNumberId:TextView;
    lateinit var campusDetailsId:TextView;
    lateinit var daysDetailsId:TextView;
    lateinit var timingDetailsId:TextView;

    lateinit var instructorNameId:TextView;
    lateinit var instructorEmailId:TextView;
    lateinit var instructorPhoneId:TextView;
    lateinit var instructorOfficeId:TextView;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_course)
        setupTextViews()
        if (NetworkUtil.isOnline(this)){
            val extras = intent.extras
            val courseID: String?
            if (extras != null) {
                courseID = extras.getString("COURSE_ID")
                val repository = repository()
                val viewModelFactory = CourseOutlineViewModelFactory(repository)
                courseOutlineViewModel = ViewModelProvider(this, viewModelFactory).get(CourseOutlineViewModel::class.java)

                if (!courseID.isNullOrEmpty()){
                    val courseNumber = courseID.split(" ").get(1)
                    courseOutlineViewModel.getCourseOutline("course-outlines?current/current/cmpt/${courseNumber}/d100")
                    courseOutlineViewModel.myOutlineReponse.observe(this) { response ->
                        if (response.isSuccessful) {
                            updateTextViewsWithResponse(response.body())
                        }
                    }
                }
            }
        }
        else{
            Toast.makeText(this, "Please connect to the Internet", Toast.LENGTH_SHORT).show()
            setContentView(R.layout.activity_offline)
            val closeBtn: Button = findViewById(R.id.close_btn_id)
            closeBtn.setOnClickListener {
                finish()
            }
        }
    }

    private fun updateTextViewsWithResponse(body: CourseOutline?) {
        if (body != null) {
            updateInfoTextView(body.info)
            updateCourseScheduleTextView(body.courseSchedule)
            updateInstructorTextView(body.instructor)
        }
    }

    private fun updateInfoTextView(info: Info?) {
        if (info != null) {
            singlecourseNameId.text = info.name.toString()
            titleId.text = stringBuilder("Course Title", info.title)
            termId.text = stringBuilder("Term", info.term)
            prereqId.text = stringBuilder("Pre-Requisities", info.prerequisites)
            unitsId.text = stringBuilder("Units", info.units)
            registrationNotesId.text = Html.fromHtml( info.registrarNotes, Html.FROM_HTML_MODE_LEGACY)
            courseDetailsId.text = Html.fromHtml( info.description, Html.FROM_HTML_MODE_LEGACY)
        }
    }
    private fun stringBuilder(prefix:String, data:String?): String{
        return "${prefix}: ${data}"
    }

    private fun updateCourseScheduleTextView(courseSchedule: List<CourseSchedule>?) {
        roomNumberId.text = stringBuilder("Room Number", courseSchedule?.get(0)?.roomNumber)
        campusDetailsId.text = stringBuilder("Campus", courseSchedule?.get(0)?.campus)
        daysDetailsId.text = stringBuilder("Days", courseSchedule?.get(0)?.days)
        timingDetailsId.text = stringBuilder("Timings", buildString {
            append(courseSchedule?.get(0)?.startTime)
            append("-")
            append(courseSchedule?.get(0)?.endTime)
        })
    }

    private fun updateInstructorTextView(instructor: List<Instructor>?) {
        instructorNameId.text = stringBuilder("Instructor Name", instructor?.get(0)?.name)
        instructorEmailId.text  = stringBuilder("Email", instructor?.get(0)?.email)
        instructorPhoneId.text = stringBuilder("Phone Number", instructor?.get(0)?.phone)
        instructorOfficeId.text = stringBuilder("Office", instructor?.get(0)?.office)
    }

    private fun setupTextViews() {
        //Course Info Text Views
        singlecourseNameId = findViewById(R.id.singlecourseNameId)
        titleId = findViewById(R.id.titleId)
        termId = findViewById(R.id.termId)
        prereqId = findViewById(R.id.prereqId)
        unitsId = findViewById(R.id.unitsId)
        registrationNotesId = findViewById(R.id.registrationNotesId)
        courseDetailsId = findViewById(R.id.courseDetailsId)

        //Course Schedule Text Views
        roomNumberId = findViewById(R.id.roomNumberId)
        campusDetailsId = findViewById(R.id.campusDetailsId)
        daysDetailsId = findViewById(R.id.daysDetailsId)
        timingDetailsId = findViewById(R.id.timingDetailsId)

        // Instructor Text Views
        instructorNameId = findViewById(R.id.instructorNameId)
        instructorEmailId = findViewById(R.id.instructorEmailId)
        instructorPhoneId = findViewById(R.id.instructorPhoneId)
        instructorOfficeId = findViewById(R.id.instructorOfficeId)
    }
}