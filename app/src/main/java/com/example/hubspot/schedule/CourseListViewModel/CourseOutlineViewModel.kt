package com.example.hubspot.schedule.CourseListViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hubspot.schedule.Models.CourseOutline
import com.example.hubspot.schedule.Repository.repository
import kotlinx.coroutines.launch
import retrofit2.Response

// Helps in making HTTPS API calls
class CourseOutlineViewModel(private val repository: repository):ViewModel() {
    val myOutlineReponse: MutableLiveData<Response<CourseOutline>> = MutableLiveData()
    fun getCourseOutline(courseNumber: String){
        viewModelScope.launch {
            val response = repository.getCourseOutline(courseNumber)
            myOutlineReponse.value = response
        }
    }
}