package com.example.hubspot.schedule.Models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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