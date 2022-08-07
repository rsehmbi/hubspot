package com.example.hubspot.ratings.ProfessorListViewModel
/**
 * The single entity of a Professor to be retrieved from the firebase database
 */
class Professor(
    var area: String,
    var department: String,
    var email: String,
    var imgUrl: String,
    var occupation: String,
    var rating: Float,
    var profName: String
) {
}