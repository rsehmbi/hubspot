package com.example.hubspot.ratings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class ReviewAddFragment : Fragment() {

    private lateinit var profRatingBar: RatingBar
    private lateinit var userCommentEditText: EditText
    private lateinit var saveReviewBtn: Button
    private lateinit var cancelReviewBtn: Button

    private val currUserId = Auth.getCurrentUser()!!.id

    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
    private var reviewExists = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_add_new_review, container, false)

        // getting reference of all the views
        getReference(view)

        // handles clicks on save/cancel
        onClickButtonHandler()

        val selectedProfName = arguments?.getString("PROF_NAME")


//         check to see if the user has already entered comment for this professor
//             if so load the data from the database, set it to the views, let users edit it
        dbReference.child("Users/${currUserId}").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.hasChild("Reviews")){
                        if(dataSnapshot.child("Reviews").hasChild(selectedProfName!!)){
                            Toast.makeText(requireActivity(), "Edit your review for ${changeDisplayName(selectedProfName)}", Toast.LENGTH_SHORT).show()
                            // set database values to the view
                            loadRating(dataSnapshot, selectedProfName)
                        }
                        else{
                            Toast.makeText(requireActivity(), "Enter your review for ${changeDisplayName(selectedProfName)}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })

            // if no: let the add a new review ==> only allow the to enter review when both rating and comment is entered
                // and save it to the database once saved is clicked

        return view
    }

    private fun getReference(view: View){
        profRatingBar = view.findViewById(R.id.ratingBar_new_rating_id)
        userCommentEditText = view.findViewById(R.id.editText_user_comment)
        saveReviewBtn = view.findViewById(R.id.save_review_btn_id)
        cancelReviewBtn = view.findViewById(R.id.cancel_review_btn_id)
    }

    private fun onClickButtonHandler() {
        saveReviewBtn.setOnClickListener {
        // check if comment is entered. Default of rating is 0
            if(userCommentEditText.text.isNullOrEmpty()){
                Toast.makeText(requireActivity(), "The comment cannot be empty!", Toast.LENGTH_SHORT).show()
            }
            else{
                // save the entries to the database
                    // then go back to displaying reviews
                val selectedProfName = arguments?.getString("PROF_NAME")

                // add the review to the user object in db
                dbReference.child("Users/${currUserId}").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.child("Reviews").hasChild(selectedProfName!!)){
                                // update the entries
                                val review = dataSnapshot.child("Reviews").child(selectedProfName)
                                review.child("Rate").ref.setValue(profRatingBar.rating)
                                review.child("Comment").ref.setValue(userCommentEditText.text.toString())
                            }
                            else{
                                println("DEBUG: adding new value to prof HERE")
                                val review = dbReference.child("Users/${currUserId}/Reviews/$selectedProfName")
                                review.child("Rate").setValue(profRatingBar.rating)
                                review.child("Comment").setValue(userCommentEditText.text.toString())
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }

                    })

//                // replacing the fragment to display reviews
//                val reviewListFragments = ReviewDisplayFragment()
//                val arg = Bundle()
//                arg.putString("PROF_NAME", selectedProfName)
//                reviewListFragments.arguments = arg
//                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_layout_3_displays,reviewListFragments).commit()
            }
        }
        cancelReviewBtn.setOnClickListener {
                // go back to displaying the reviews
            // changing parent activity's button
            val rateReviewBtn = requireActivity().findViewById<Button>(com.example.hubspot.R.id.rate_now_btn_id)
            rateReviewBtn.text = "Rate Now!"

            // replacing the fragment to display reviews
            val selectedProfName = arguments?.getString("PROF_NAME")
            val reviewListFragments = ReviewDisplayFragment()
            val arg = Bundle()
            arg.putString("PROF_NAME", selectedProfName)
            reviewListFragments.arguments = arg
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_layout_3_displays,reviewListFragments).commit()
        }
    }

    // load the previously entered review to the view
    private fun loadRating(dataSnapshot: DataSnapshot, selectedProfName: String){
        // user has already commented for selectedProfName
        reviewExists = true
        // load previous rate and comment
        val comment = dataSnapshot.child("Reviews").child(selectedProfName).child("Comment").value.toString()
        val rate = dataSnapshot.child("Reviews").child(selectedProfName).child("Rate").value.toString()
        // set views to previous values
        profRatingBar.rating = rate.toFloat()
        userCommentEditText.setText(comment)
    }

    // to capitalize first letter of prof's first and last name
    private fun changeDisplayName(name: String): String{
        return name.split(" ").joinToString(" ") { it ->
            it.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    }
}