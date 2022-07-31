package com.example.hubspot.schedule.API

import com.example.hubspot.schedule.Models.CourseOutline
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Headers

interface SimpleApi {

    @Headers("Content-Type: application/json")
    @GET("course-outlines?current/current/cmpt/362/d100")
    suspend fun getCourseOutline(): Response<CourseOutline>
}