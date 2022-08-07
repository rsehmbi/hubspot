package com.example.hubspot.ratings.ProfessorListViewModel

/**
 * The single entity of a Review to be retrieved from and stored in the firebase database
 */
data class Review(var comment: String, var rating: Float)