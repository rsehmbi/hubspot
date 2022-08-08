package com.example.hubspot.schedule.API

import com.example.hubspot.schedule.Models.CourseOutline
import retrofit2.Response
import retrofit2.http.*

// Simple API interface to make get requests for grabbing course information
interface SimpleApi {

    @GET
    suspend fun getCourseOutline(@Url number: String): Response<CourseOutline>
}