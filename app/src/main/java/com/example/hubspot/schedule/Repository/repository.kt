package com.example.hubspot.schedule.Repository

import com.example.hubspot.schedule.API.RetrofitInstance
import com.example.hubspot.schedule.Models.CourseOutline
import retrofit2.Response

// Course API repository
class repository {
    suspend fun getCourseOutline(courseNumber: String): Response<CourseOutline> {
        return RetrofitInstance.api.getCourseOutline(courseNumber)
    }
}