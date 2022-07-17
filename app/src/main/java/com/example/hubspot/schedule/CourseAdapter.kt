package com.example.hubspot.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import kotlin.collections.ArrayList


class CourseAdapter(private val dataSet: ArrayList<Course>) :
    RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseCodeTextView: TextView;
        val courseNameTextView: TextView;
        val courseDescriptionTextView: TextView;
        val courseLocationTextView: TextView;
        val courseProfessorTextView: TextView;
        val courseCreditsTextView: TextView;

        init {
            courseCodeTextView = view.findViewById(R.id.courseCodeId)
            courseNameTextView = view.findViewById(R.id.courseNameId)
            courseDescriptionTextView = view.findViewById(R.id.descriptionId)
            courseLocationTextView = view.findViewById(R.id.courseLocationId)
            courseProfessorTextView = view.findViewById(R.id.professNameId)
            courseCreditsTextView = view.findViewById(R.id.creditsId)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.course, viewGroup, false)

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
    }

    private fun setCourseCode(viewHolder: ViewHolder, selectedCourse: Course) {
        viewHolder.courseCodeTextView.text = selectedCourse.courseCode
    }

    override fun getItemCount() = dataSet.size

}