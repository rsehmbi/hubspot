package com.example.hubspot.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.google.firebase.database.*


class ScheduleFragment : Fragment() {
    lateinit var autocompleteTextSearch: AutoCompleteTextView;
    lateinit var autoPopulateCourseList: RecyclerView;
    val SuggestionCourselist = ArrayList<String>()
    var SelectedCourselist = ArrayList<Course>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scheduleView: View = inflater.inflate(R.layout.fragment_schedule, container, false)
        autocompleteTextSearch = scheduleView.findViewById(R.id.autoCompleteId)
        autoPopulateCourseList = scheduleView.findViewById(R.id.courseListViewId)
        val recycleLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        autoPopulateCourseList.layoutManager = recycleLayoutManager


        val courseReference =
            FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference.child(
                "Courses"
            )
        loadSuggestionList(courseReference)

        return scheduleView
    }

    private fun loadSuggestionList(databaseReference: DatabaseReference) {
        databaseReference.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    println("Data snapshot is" + dataSnapshot.value)
                    for (dsp in dataSnapshot.children) {
                        val coursekey = dsp.key
                        SuggestionCourselist.add(coursekey!!) //add result into array list
                    }
                    var courseListAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        SuggestionCourselist
                    )
                    autocompleteTextSearch.setAdapter(courseListAdapter)
                    autocompleteTextSearch.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
                        val courseSelected = parent.selectedItem
                        loadCourseInfo(databaseReference, courseSelected.toString())
                        Toast.makeText(requireContext(), "${courseSelected}", Toast.LENGTH_SHORT).show()
                    }
                    println("Data snapshot is" + SuggestionCourselist)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //handle databaseError
                }
            })
    }

    private fun loadCourseInfo(databaseReference:DatabaseReference, courseSelected: String) {
        val query: Query = databaseReference.orderByChild("CourseCode").equalTo(courseSelected)
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        val selectedCourse: Course = Course(
                            dataSnapshot.child("ProfessorName").value.toString(),
                            dataSnapshot.child("CourseName").value.toString(),
                            dataSnapshot.child("CourseCode").value.toString(),
                            dataSnapshot.child("Description").value.toString(),
                            dataSnapshot.child("Credits").value.toString(),
                            dataSnapshot.child("Location").value.toString(),

                        )
                        SelectedCourselist.add(selectedCourse)
                    }
                    val adapter = CourseAdapter(SelectedCourselist)
                    autoPopulateCourseList.adapter = adapter
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        query.addListenerForSingleValueEvent(valueListener)
    }
}

class Course(var professorName: String,
             var courseName:String,
             var courseCode:String,
             var courseDescription: String,
             var credits: String,
             var location:String)