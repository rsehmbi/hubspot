package com.example.hubspot.schedule.Models

//data class CourseSchedule(
//    val roomNumber:String,
//    val campus:String,
//    val startTime: String,
//    val endTime: String,
//)
//
//data class CourseOutline (
//    val courseSchedule: ArrayList<CourseSchedule>,
//)

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

class Info {
    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("deliveryMethod")
    @Expose
    var deliveryMethod: String? = null

    @SerializedName("corequisites")
    @Expose
    var corequisites: String? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("section")
    @Expose
    var section: String? = null

    @SerializedName("dept")
    @Expose
    var dept: String? = null

    @SerializedName("units")
    @Expose
    var units: String? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("classNumber")
    @Expose
    var classNumber: String? = null

    @SerializedName("departmentalUgradNotes")
    @Expose
    var departmentalUgradNotes: String? = null

    @SerializedName("prerequisites")
    @Expose
    var prerequisites: String? = null

    @SerializedName("number")
    @Expose
    var number: String? = null

    @SerializedName("registrarNotes")
    @Expose
    var registrarNotes: String? = null

    @SerializedName("degreeLevel")
    @Expose
    var degreeLevel: String? = null

    @SerializedName("outlinePath")
    @Expose
    var outlinePath: String? = null

    @SerializedName("specialTopic")
    @Expose
    var specialTopic: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("term")
    @Expose
    var term: String? = null

    @SerializedName("designation")
    @Expose
    var designation: String? = null
}

class Instructor {
    @SerializedName("profileUrl")
    @Expose
    var profileUrl: String? = null

    @SerializedName("commonName")
    @Expose
    var commonName: String? = null

    @SerializedName("firstName")
    @Expose
    var firstName: String? = null

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null

    @SerializedName("phone")
    @Expose
    var phone: String? = null

    @SerializedName("roleCode")
    @Expose
    var roleCode: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("officeHours")
    @Expose
    var officeHours: String? = null

    @SerializedName("office")
    @Expose
    var office: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null
}