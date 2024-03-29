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
import com.example.hubspot.schedule.CourseListViewModel.UserCourseViewModel
import com.example.hubspot.schedule.SingleCourseActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.lang.Integer.parseInt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class SingleCourseAdapter(private val dataSet: ArrayList<Course>, private val userCourseViewModel: UserCourseViewModel) :
    RecyclerView.Adapter<SingleCourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseCodeTextView: TextView;
        val courseNameTextView: TextView;
        val courseDescriptionTextView: TextView;
        val courseLocationTextView: TextView;
        val courseProfessorTextView: TextView;
        val courseCreditsTextView: TextView;
        val addToCalendar: Button;
        val unenrollButton: Button

        init {
            courseCodeTextView = view.findViewById(R.id.courseCodeId)
            courseNameTextView = view.findViewById(R.id.courseNameId)
            courseDescriptionTextView = view.findViewById(R.id.descriptionId)
            courseLocationTextView = view.findViewById(R.id.courseLocationId)
            courseProfessorTextView = view.findViewById(R.id.professNameId)
            courseCreditsTextView = view.findViewById(R.id.creditsId)
            addToCalendar = view.findViewById(R.id.addToCalendarId)
            unenrollButton = view.findViewById(R.id.unenrollButtonId)
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

        // Attaches on click listener for button in adapter views
        viewHolder.addToCalendar.setOnClickListener { it->
            addCourseToCalendar(it.context, selectedCourse)
        }

        // Attaches on click listener for button in adapter views
        viewHolder.unenrollButton.setOnClickListener {
            dataSet.remove(selectedCourse)
            userCourseViewModel.enrolledCourseList.remove(selectedCourse)
            removeCourse(selectedCourse.courseCode, userCourseViewModel)
            notifyDataSetChanged()
            Toast.makeText(it.context, "Un-enrolled from ${selectedCourse.courseCode}", Toast.LENGTH_SHORT).show()
        }

        // Attaches on click listener for button in adapter views
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(it.context, SingleCourseActivity::class.java).apply {
                putExtra("COURSE_ID", selectedCourse.courseCode)
            }
            it.context.startActivity(intent)
        }
    }

    // Remove the course that user is currently enrolled in. It will unenroll the person from the course currently selected by user
    private fun removeCourse(courseCode: String, userCourseViewModel: UserCourseViewModel) {
        userCourseViewModel.usersCoursesReference.child("Courses").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    if(ds.getValue(String::class.java) == courseCode)
                    {
                        ds.ref.removeValue()
                        for (course in userCourseViewModel.enrolledCourseList){
                            if(course.courseCode == courseCode){
                                userCourseViewModel.enrolledCourseList.remove(course)
                            }
                        }
                        userCourseViewModel.courseList?.value?.remove(courseCode)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    // Manipulates the calendar for different timings
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

    // Get the course end time based on how long the class is
    private fun getEndTime(course: Course): Long {
        val calendar = getCalendar(course)
        return calendar.getTimeInMillis() + parseInt(course.courseDuration) * 60 * 60 * 1000
    }

    // Get the course start time
    private fun getStartTime(course:Course): Long {
        val calendar = getCalendar(course)
        return calendar.getTimeInMillis()
    }

    // Create a calendar intent to directly add course info to the calendar
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