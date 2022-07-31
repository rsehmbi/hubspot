package com.example.hubspot.schedule.Models
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CourseOutline {
    @SerializedName("info")
    @Expose
    var info: Info? = null

    @SerializedName("instructor")
    @Expose
    var instructor: List<Instructor>? = null

    @SerializedName("courseSchedule")
    @Expose
    var courseSchedule: List<CourseSchedule>? = null
}