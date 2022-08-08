package com.example.hubspot.schedule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.schedule.Adapters.SingleCourseAdapter
import com.example.hubspot.schedule.CourseListViewModel.Course
import com.example.hubspot.schedule.CourseListViewModel.CourseListViewModel
import com.example.hubspot.schedule.CourseListViewModel.UserCourseViewModel
import com.google.firebase.database.*

class ShowMySchedule : Fragment() {
    lateinit var mycourseListView: RecyclerView
    lateinit var usercourseListViewModel: UserCourseViewModel
    lateinit var courseListViewModel: CourseListViewModel
    private var SelectedCourselist = ArrayList<String>()

    // This fragment is reponsible showing enrolled courses that the user is enrolled in
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myscheduleView: View = inflater.inflate(R.layout.activity_show_my_schedule, container, false)

        mycourseListView = myscheduleView.findViewById(R.id.myscheduleListView)
        val recycleLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireActivity())
        mycourseListView.layoutManager = recycleLayoutManager

        usercourseListViewModel = ViewModelProvider(this)[UserCourseViewModel::class.java]
        courseListViewModel = ViewModelProvider(this)[CourseListViewModel::class.java]
        usercourseListViewModel.getUserCourses()?.observe(requireActivity()) {
            SelectedCourselist = it
            loadEachCourseinRecyclerView(courseListViewModel.courseReference, SelectedCourselist)
        }
        return myscheduleView
    }

    private fun loadEachCourseinRecyclerView(
        CoursesReference: DatabaseReference,
        SelectedCourselist: ArrayList<String>
    ) {
        for (courseSelected in SelectedCourselist){
            val query: Query = CoursesReference.orderByChild("CourseCode").equalTo(courseSelected)
            val valueListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (dataSnapshot in snapshot.children){
                            val selectedCourse = Course(
                                dataSnapshot.child("ProfessorName").value.toString(),
                                dataSnapshot.child("CourseName").value.toString(),
                                dataSnapshot.child("CourseCode").value.toString(),
                                dataSnapshot.child("Description").value.toString(),
                                dataSnapshot.child("Credits").value.toString(),
                                dataSnapshot.child("Location").value.toString(),
                                dataSnapshot.child("courseStartDateTime").value.toString(),
                                dataSnapshot.child("courseEndDate").value.toString(),
                                dataSnapshot.child("courseDuration").value.toString(),
                                dataSnapshot.child("courseDays").value.toString(),
                            )
                            if (!alreadyExists(selectedCourse)){
                                usercourseListViewModel.enrolledCourseList.add(selectedCourse)
                            }
                        }
                        val adapter = SingleCourseAdapter(usercourseListViewModel.enrolledCourseList, usercourseListViewModel)
                        mycourseListView.adapter = adapter
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }

            }
            query.addListenerForSingleValueEvent(valueListener)
        }
    }

    private fun alreadyExists(course: Course): Boolean{
        for (enrolledCourse in usercourseListViewModel.enrolledCourseList){
            if (course.courseCode == enrolledCourse.courseCode){
                return true
            }
        }
        return false
    }
}


