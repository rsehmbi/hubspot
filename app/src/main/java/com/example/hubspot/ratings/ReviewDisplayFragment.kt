package com.example.hubspot.ratings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.ratings.ProfessorListViewModel.ProfessorListViewModel
import com.example.hubspot.ratings.ProfessorListViewModel.Review
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 * Use the [ReviewDisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReviewDisplayFragment : Fragment() {

    private lateinit var profListViewModel: ProfessorListViewModel
    private lateinit var reviewArrayList: ArrayList<Review>
    private lateinit var reviewListView: ListView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_reviews_display, container, false)
        // get the comments from the data base
        reviewArrayList = ArrayList()
        reviewListView = view.findViewById<ListView>(R.id.review_listView)

        var passedProfName: String? = arguments?.getString("PROF_NAME")

        profListViewModel = ViewModelProvider(this)[ProfessorListViewModel::class.java]
        profListViewModel.professorReference.child("$passedProfName/Reviews").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for(review in dataSnapshot.children) {

                        val rating = review.child("Rating").value.toString().toFloat()
                        val review = Review(review.child("Comment").value.toString(), rating)
                        reviewArrayList.add(review)
                    }
                    if(reviewArrayList.size == 0){
                        val reviewStat = view.findViewById<TextView>(R.id.comment_stat_id)
                        reviewStat.text = "Nothing to show. Leave the first review!"
                    }
                    else{
                        reviewListView.isClickable = false

                        // to avoid exception when swapping fragments very fast
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