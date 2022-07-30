package com.example.hubspot.schedule.CourseListViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hubspot.auth.Auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserCourseViewModel:ViewModel() {
    val currentUser = Auth.getCurrentUser()?.id
    var courseList: MutableLiveData<ArrayList<String>>? = null
    var enrolledCourseList = ArrayList<Course>()

    val usersCoursesReference = FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference.child(
        "Users").child(currentUser.toString())

    fun getUserCourses(): LiveData<ArrayList<String>>? {
        if (courseList == null) {
            courseList = MutableLiveData<ArrayList<String>>()
            loadUserCourses()
        }
        return courseList
    }

    private fun loadUserCourses(){
        usersCoursesReference.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userCourseList: ArrayList<String> = arrayListOf()
                    if (dataSnapshot.hasChild("Courses")){
                        for (dsp in dataSnapshot.child("Courses").children) {
                            val coursevalue = dsp.value
                            userCourseList.add(coursevalue!! as String)
                        }
                        courseList?.value = userCourseList
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    //handle databaseError
                }
            })
    }

}