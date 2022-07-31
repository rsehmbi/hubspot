package com.example.hubspot.schedule.Models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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