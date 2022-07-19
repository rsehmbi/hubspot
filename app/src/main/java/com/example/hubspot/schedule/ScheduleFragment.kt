package com.example.hubspot.schedule

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.schedule.CourseListViewModel.Course
import com.example.hubspot.schedule.CourseListViewModel.CourseListViewModel
import com.google.firebase.database.*


class ScheduleFragment : Fragment() {
    lateinit var autocompleteTextSearch: AutoCompleteTextView;
    lateinit var autoPopulateCourseList: RecyclerView;
    private var SuggestionCourselist = ArrayList<String>()
    lateinit var courseListViewModel: CourseListViewModel;

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
        courseListViewModel.getCourseListSuggestions()?.observe(requireActivity(), Observer {
            SuggestionCourselist = it
            var courseListAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_list_item_1,
                SuggestionCourselist
            )
            autocompleteTextSearch.setAdapter(courseListAdapter)
            autocompleteTextSearch.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
                val courseSelected = parent.selectedItem
                loadCourseInfo(courseListViewModel.courseReference, courseSelected.toString())
                Toast.makeText(requireContext(), "Course ${courseSelected} added to cart", Toast.LENGTH_SHORT).show()
                autocompleteTextSearch.getText().clear()
            }
            val adapter = CourseAdapter(courseListViewModel.SelectedCourselist)
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
                                )
                            courseListViewModel.SelectedCourselist.add(selectedCourse)
                        }
                        val adapter = CourseAdapter(courseListViewModel.SelectedCourselist)
                        autoPopulateCourseList.adapter = adapter
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            query.addListenerForSingleValueEvent(valueListener)
        }
    }

    private fun onClickButtonHandler(view: View){
        view.findViewById<Button>(R.id.enroll_button_id).setOnClickListener {
            Toast.makeText(requireContext(), "Enroll feature in progress", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.view_schedule_id).setOnClickListener {
            Toast.makeText(requireContext(), "View Schedule feature in progress", Toast.LENGTH_SHORT).show()
        }

    }
}
