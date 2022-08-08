package com.example.hubspot.schedule.Models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

// POJO class for Course Schedule
class CourseSchedule {
    @SerializedName("roomNumber")
    @Expose
    var roomNumber: String? = null

    @SerializedName("endDate")
    @Expose
    var endDate: String? = null

    @SerializedName("campus")
    @Expose
    var campus: String? = null

    @SerializedName("buildingCode")
    @Expose
    var buildingCode: String? = null

    @SerializedName("days")
    @Expose
    var days: String? = null

    @SerializedName("sectionCode")
    @Expose
    var sectionCode: String? = null

    @SerializedName("startTime")
    @Expose
    var startTime: String? = null

    @SerializedName("isExam")
    @Expose
    var isExam: Boolean? = null

    @SerializedName("endTime")
    @Expose
    var endTime: String? = null

    @SerializedName("startDate")
    @Expose
    var startDate: String? = null
}