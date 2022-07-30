package com.example.hubspot.schedule.Adapters

import com.example.hubspot.schedule.SingleCourseActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.schedule.CourseListViewModel.Course
import kotlin.collections.ArrayList



class SingleCourseAdapter(private val dataSet: ArrayList<Course>) :
    RecyclerView.Adapter<SingleCourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseCodeTextView: TextView;
        val courseNameTextView: TextView;
        val courseDescriptionTextView: TextView;
        val courseLocationTextView: TextView;
        val courseProfessorTextView: TextView;
        val courseCreditsTextView: TextView;
        val addToCalendar: Button;
        val downloadCourseOutline: Button;

        init {
            courseCodeTextView = view.findViewById(R.id.courseCodeId)
            courseNameTextView = view.findViewById(R.id.courseNameId)
            courseDescriptionTextView = view.findViewById(R.id.descriptionId)
            courseLocationTextView = view.findViewById(R.id.courseLocationId)
            courseProfessorTextView = view.findViewById(R.id.professNameId)
            courseCreditsTextView = view.findViewById(R.id.creditsId)
            addToCalendar = view.findViewById(R.id.addToCalendarId)
            downloadCourseOutline = view.findViewById(R.id.downloadOutlineId)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.mycourse, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val selectedCourse = dataSet.get(position)
        setCourseCode(viewHolder, selectedCourse)
        viewHolder.courseCreditsTextView.text = selectedCourse.credits
        viewHolder.courseLocationTextView.text = selectedCourse.location
        viewHolder.courseNameTextView.text = selectedCourse.courseName
        viewHolder.courseProfessorTextView.text = selectedCourse.professorName
        viewHolder.courseDescriptionTextView.text = "Description: ${selectedCourse.courseDescription}"

        viewHolder.downloadCourseOutline.setOnClickListener { i->
            Toast.makeText(
                viewHolder.itemView.context,
                "test",
                Toast.LENGTH_SHORT
            ).show() }

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(it.context, SingleCourseActivity::class.java).apply {
                putExtra("COURSE_ID", selectedCourse.courseCode)
            }
            it.context.startActivity(intent)
        }
    }

    private fun setCourseCode(viewHolder: ViewHolder, selectedCourse: Course) {
        viewHolder.courseCodeTextView.text = selectedCourse.courseCode
    }

    override fun getItemCount() = dataSet.size

}