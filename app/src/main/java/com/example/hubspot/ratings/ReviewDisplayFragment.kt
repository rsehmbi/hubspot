package com.example.hubspot.ratings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.ratings.ProfessorListViewModel.Review
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A ReviewDisplayFragment is [Fragment] subclass that handles the display of reviews as they get
 * updated
 */
class ReviewDisplayFragment : Fragment() {

    private lateinit var reviewArrayList: ArrayList<Review>
    private lateinit var reviewListView: ListView

    private val currUserId = Auth.getCurrentUser()!!.id

    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_reviews_display, container, false)
        // get the comments from the data base
        reviewArrayList = ArrayList()
        reviewListView = view.findViewById(R.id.review_listView)

        var passedProfName: String? = arguments?.getString("PROF_NAME")




        // get reviews from database
        var userDisplayName = "Unknown User:"
        dbReference.child("Professors/$passedProfName").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    reviewArrayList.clear()
                    if(dataSnapshot.hasChild("Reviews")){
                        for(userReview in dataSnapshot.child("Reviews").children) {
                            val res = userReview.value as Map<*, *>
                            println("bug: $res")
                            if(res["Comment"] != null && res["Rate"]!= null){
                                val review = Review(res["Comment"]!!.toString(), res["Rate"]!!.toString().toFloat())
                                reviewArrayList.add(review)
                            }
                        }
                    }
                    if(reviewArrayList.size == 0){
                        val reviewStat = view.findViewById<TextView>(R.id.comment_stat_id)
                        reviewStat.text = "Nothing to show. Leave the first review!"
                    }
                    else{
                        reviewListView.isClickable = false

                        // to avoid crashing when swapping fragments very fast
                        if(isAdded){
                            reviewListView.adapter = ReviewAdapter(requireActivity(), reviewArrayList)
                        }
                    }
                }
                override fun onCancelled(p0: DatabaseError) {}
            })


        return view
    }

}