package com.example.hubspot.schedule.CourseListViewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CourseListViewModel:ViewModel() {
    private var courseListSuggestions: MutableLiveData<ArrayList<String>>? = null
    var SelectedCourselist = ArrayList<Course>()
    val courseReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference.child(
            "Courses"
        )

    fun getCourseListSuggestions(): LiveData<ArrayList<String>>? {
        if (courseListSuggestions == null) {
            courseListSuggestions = MutableLiveData<ArrayList<String>>()
            loadSuggestions()
        }
        return courseListSuggestions
    }

    private fun loadSuggestions(){
        courseReference.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var suggestions = ArrayList<String>()
                    for (dsp in dataSnapshot.children) {
                        val coursekey = dsp.key
                        suggestions.add(coursekey!!)
                    }
                    courseListSuggestions?.value = suggestions
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //handle databaseError
                }
            })
    }

}