package com.example.hubspot.ratings

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.example.hubspot.R
import com.example.hubspot.ratings.ProfessorListViewModel.Review
import kotlin.collections.ArrayList

class ReviewAdapter(private val context: Activity, private val reviewArrayList: ArrayList<Review>) : ArrayAdapter<Review>(context, R.layout.reviews, reviewArrayList){
    private lateinit var ratingBar: RatingBar
    private lateinit var commentView: TextView

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = inflater.inflate(R.layout.reviews, null)

        ratingBar = view.findViewById(R.id.rating_bar_display_id)
        commentView = view.findViewById(R.id.display_comment_view_id)

        ratingBar.rating = reviewArrayList[position].rating
        commentView.text = reviewArrayList[position].comment

        return view
    }
}