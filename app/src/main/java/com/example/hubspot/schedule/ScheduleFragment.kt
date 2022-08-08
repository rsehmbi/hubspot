package com.example.hubspot.schedule

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.schedule.CourseListViewModel.Course
import com.example.hubspot.schedule.CourseListViewModel.CourseListViewModel
import com.example.hubspot.schedule.CourseListViewModel.UserCourseViewModel
import com.google.firebase.database.*


class ScheduleFragment : Fragment() {
    lateinit var autocompleteTextSearch: AutoCompleteTextView;
    lateinit var autoPopulateCourseList: RecyclerView;
    private var SuggestionCourselist = ArrayList<String>()
    lateinit var courseListViewModel: CourseListViewModel;
    lateinit var usercourseListViewModel: UserCourseViewModel;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scheduleView: View = inflater.inflate(R.layout.fragment_schedule, container, false)
        onClickButtonHandler(scheduleView)
        autocompleteTextSearch = scheduleView.findViewById(R.id.autoCompleteId)
        autoPopulateCourseList = scheduleView.findViewById(R.id.courseListViewId)
        val recycleLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        autoPopulateCourseList.layoutManager = recycleLayoutManager

        courseListViewModel = ViewModelProvider(requireActivity())[CourseListViewModel::class.java]
        usercourseListViewModel = ViewModelProvider(this)[UserCourseViewModel::class.java]
        usercourseListViewModel.getUserCourses()
        courseListViewModel.getCourseListSuggestions()?.observe(requireActivity(), Observer {
            SuggestionCourselist = it
            val courseListAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_list_item_1,
                SuggestionCourselist
            )
            autocompleteTextSearch.setAdapter(courseListAdapter)
            autocompleteTextSearch.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
                val courseSelected = parent.getItemAtPosition(position)
                loadCourseInfo(courseListViewModel.courseReference, courseSelected.toString())
                Toast.makeText(requireContext(), "Course ${courseSelected} added to cart", Toast.LENGTH_SHORT).show()
                autocompleteTextSearch.getText().clear()
            }
            val adapter = CourseAdapter(courseListViewModel.SelectedCourselist, courseListViewModel)
            autoPopulateCourseList.adapter = adapter
        })
        return scheduleView
    }

    private fun checkAlreadySelected(courseSelected: String): Boolean{
        for (course in courseListViewModel.SelectedCourselist ){
            if (courseSelected == course.courseCode){
                Toast.makeText(requireActivity(), "Course Already Selected", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    // Loads course info in the recycler view
    private fun loadCourseInfo(databaseReference:DatabaseReference, courseSelected: String) {
        if (!checkAlreadySelected(courseSelected)){
            val query: Query = databaseReference.orderByChild("CourseCode").equalTo(courseSelected)
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
                            courseListViewModel.SelectedCourselist.add(selectedCourse)
                        }
                        val adapter = CourseAdapter(courseListViewModel.SelectedCourselist, courseListViewModel)
                        autoPopulateCourseList.adapter = adapter
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            query.addListenerForSingleValueEvent(valueListener)
        }
    }

    // Gets new selected courses by user
    private fun getNewCourses(): List<String> {
        val coursesToEnroll: MutableList<String> = mutableListOf()
        for (course in courseListViewModel.SelectedCourselist) {
            coursesToEnroll.add(course.courseCode)
        }
        return coursesToEnroll.distinct()
    }

    // Checks if user is already enrolled in courses, if yes, enroll in newly selected courses
    private fun enrollInCourse(){
        val UsersReference =
            FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference.child(
                "Users"
            )
        val currentUser = Auth.getCurrentUser()
        val courseList = getNewCourses()
        if (currentUser != null){
            try {
                UsersReference.addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val enrolledList: MutableList<String> = ArrayList()
                            if (dataSnapshot.child(currentUser?.id.toString()).hasChild("Courses")){
                                for (dsp in dataSnapshot.child(currentUser?.id.toString()).child("Courses").children) {
                                    val coursevalue = dsp.getValue(String::class.java)
                                    enrolledList.add(coursevalue!!)
                                }
                                for (course in courseList){
                                    enrolledList.add(course)
                                }
                                if (enrolledList.isNotEmpty()) {
                                    UsersReference.child(Auth.getCurrentUser()?.id.toString()).child("Courses")
                                        .setValue(enrolledList.distinct())
                                    Toast.makeText(requireContext(), "Course added to schedule", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(requireContext(), "No Course Selected", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                if (courseList.isNotEmpty()) {
                                    UsersReference.child(Auth.getCurrentUser()?.id.toString()).child("Courses")
                                        .setValue(courseList)
                                    Toast.makeText(requireContext(), "Course added to schedule", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(requireContext(), "No Course Selected", Toast.LENGTH_SHORT).show()
                                }
                            }
                            val showMySchedule = ShowMySchedule()
                            val transaction = activity!!.supportFragmentManager.beginTransaction()
                            transaction.replace(R.id.flContent, showMySchedule)
                            transaction.disallowAddToBackStack()
                            transaction.commit()
                        }
                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
            }
            catch (e: Exception){
                Toast.makeText(requireContext(), "Unable to add courses", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }
    private fun onClickButtonHandler(view: View){
        // Helps in Clearing Selected courses
        view.findViewById<Button>(R.id.clear_button_id).setOnClickListener {
            courseListViewModel.SelectedCourselist.clear()
            val adapter = CourseAdapter(courseListViewModel.SelectedCourselist, courseListViewModel)
            autoPopulateCourseList.adapter = adapter
        }
        // Helps in Course Enrollment
        view.findViewById<Button>(R.id.enroll_button_id).setOnClickListener {
            enrollInCourse()
        }
    }
}
