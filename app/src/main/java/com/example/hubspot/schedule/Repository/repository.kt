package com.example.hubspot.schedule.Repository

import com.example.hubspot.schedule.API.RetrofitInstance
import com.example.hubspot.schedule.Models.CourseOutline
import retrofit2.Response

class repository {
    suspend fun getCourseOutline(): Response<CourseOutline> {
        return RetrofitInstance.api.getCourseOutline()
    }
}