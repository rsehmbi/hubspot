package com.example.hubspot.schedule.Adapters

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.schedule.CourseListViewModel.Course
import com.example.hubspot.schedule.SingleCourseActivity
import java.lang.Integer.parseInt
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries.localDate
import java.util.*


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

        init {
            courseCodeTextView = view.findViewById(R.id.courseCodeId)
            courseNameTextView = view.findViewById(R.id.courseNameId)
            courseDescriptionTextView = view.findViewById(R.id.descriptionId)
            courseLocationTextView = view.findViewById(R.id.courseLocationId)
            courseProfessorTextView = view.findViewById(R.id.professNameId)
            courseCreditsTextView = view.findViewById(R.id.creditsId)
            addToCalendar = view.findViewById(R.id.addToCalendarId)
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

        viewHolder.addToCalendar.setOnClickListener { it->
            addCourseToCalendar(it.context, selectedCourse)
        }

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(it.context, SingleCourseActivity::class.java).apply {
                putExtra("COURSE_ID", selectedCourse.courseCode)
            }
            it.context.startActivity(intent)
        }
    }
    private fun getCalendar(course: Course): Calendar {
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val calendar: Calendar = Calendar.getInstance()
        val localDateTime = LocalDateTime.parse(course.courseStartDateTime, pattern)
        calendar
            .set(localDateTime.year,
                Calendar.MARCH,
                localDateTime.dayOfMonth,
                localDateTime.hour ,
                localDateTime.minute, 0)
        return calendar
    }

    private fun getEndTime(course: Course): Long {
        val calendar = getCalendar(course)
        return calendar.getTimeInMillis() + parseInt(course.courseDuration) * 60 * 60 * 1000
    }

    private fun getStartTime(course:Course): Long {
        val calendar = getCalendar(course)
        return calendar.getTimeInMillis()
    }

    private fun addCourseToCalendar(context: Context?, course: Course) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setDataAndType(Events.CONTENT_URI,"vnd.android.cursor.item/event")
            .putExtra(Events.TITLE, course.courseName)
            .putExtra(Events.DESCRIPTION, course.courseDescription)
            .putExtra(Events.EVENT_LOCATION, course.location)
            .putExtra(
                Events.RRULE,
                "FREQ=WEEKLY;BYDAY=${course.courseDays};UNTIL=${course.courseEndDate}"
            )
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, getStartTime(course))
            .putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                getEndTime(course)
            )
            .putExtra(Events.HAS_ALARM, 1)
            .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
        context?.startActivity(intent)
    }

    private fun setCourseCode(viewHolder: ViewHolder, selectedCourse: Course) {
        viewHolder.courseCodeTextView.text = selectedCourse.courseCode
    }

    override fun getItemCount() = dataSet.size

}