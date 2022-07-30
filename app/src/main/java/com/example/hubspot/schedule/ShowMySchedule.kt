package com.example.hubspot.schedule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.schedule.Adapters.SingleCourseAdapter
import com.example.hubspot.schedule.CourseListViewModel.Course
import com.example.hubspot.schedule.CourseListViewModel.CourseListViewModel
import com.example.hubspot.schedule.CourseListViewModel.UserCourseViewModel
import com.google.firebase.database.*

class ShowMySchedule : AppCompatActivity() {
    lateinit var mycourseListView: RecyclerView;
    lateinit var usercourseListViewModel: UserCourseViewModel;
    lateinit var courseListViewModel: CourseListViewModel;
    var SelectedCourselist = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_my_schedule)

        mycourseListView = findViewById(R.id.myscheduleListView)
        val recycleLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        mycourseListView.layoutManager = recycleLayoutManager

        usercourseListViewModel = ViewModelProvider(this)[UserCourseViewModel::class.java]
        courseListViewModel = ViewModelProvider(this)[CourseListViewModel::class.java]
        usercourseListViewModel.getUserCourses()?.observe(this, Observer {
            SelectedCourselist = it
            loadEachCourseinRecyclerView(courseListViewModel.courseReference, SelectedCourselist)
        })
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
                            )
                            usercourseListViewModel.enrolledCourseList.add(selectedCourse)
                        }
                        val adapter = SingleCourseAdapter(usercourseListViewModel.enrolledCourseList)
                        mycourseListView.adapter = adapter
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            query.addListenerForSingleValueEvent(valueListener)
        }

    }
}


