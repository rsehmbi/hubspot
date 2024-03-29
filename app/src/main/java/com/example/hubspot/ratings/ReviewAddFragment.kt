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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.ratings.ProfessorListViewModel.ProfessorListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

/**
 * ReviewAddFragment is a [Fragment] subclass and handles the logic of adding new reviews
 * or editing previous reviews
 */
class ReviewAddFragment : Fragment() {

    private lateinit var profRatingBar: RatingBar
    private lateinit var userCommentEditText: EditText
    private lateinit var saveReviewBtn: Button
    private lateinit var cancelReviewBtn: Button
    private lateinit var deleteReviewBtn: Button
    private var changeFragment: Boolean = false

    private val currUserId = Auth.getCurrentUser()!!.id

    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference

    private lateinit var profListViewModel: ProfessorListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_add_new_review, container, false)

        // getting reference of all the views
        getReference(view)

        // handles clicks on save/cancel
        onClickButtonHandler(view)

        val selectedProfName = arguments?.getString("PROF_NAME")

        profListViewModel = ViewModelProvider(requireActivity())[ProfessorListViewModel::class.java]


         // check to see if the user has already entered comment for this professor
             // if yes: load the data from the database, set it to the views, let users edit it
        dbReference.child("Users/${currUserId}").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.child("Reviews").hasChild(selectedProfName!!)) {
                        if(isAdded){
                            Toast.makeText(requireActivity(), "Edit your review for ${changeDisplayName(selectedProfName)}", Toast.LENGTH_SHORT).show()
                        }
                        // set database values to the view
                        loadRating(dataSnapshot, selectedProfName)
                    }
                    else{
                        if(isAdded){
                            Toast.makeText(requireActivity(), "Enter your review for ${changeDisplayName(selectedProfName)}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })

            // if no: let the add a new review ==> only allow the user to enter a review when comment is entered
                // and save it to the database once saved is clicked

        return view
    }

    override fun onResume() {
        if(changeFragment){
            profListViewModel.currentFragment = 0
            Toast.makeText(requireActivity(), "Review deleted", Toast.LENGTH_SHORT).show()
            val selectedProfName = arguments?.getString("PROF_NAME")
            changeFragment = false
            val rateReviewBtn =
                requireActivity().findViewById<Button>(R.id.rate_now_btn_id)
            rateReviewBtn.text = "Rate Now!"

            // replacing the fragment to display reviews
            val reviewListFragments = ReviewDisplayFragment()
            val arg = Bundle()
            arg.putString("PROF_NAME", selectedProfName)
            reviewListFragments.arguments = arg
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout_3_displays, reviewListFragments).commit()
        }
        super.onResume()
    }

    private fun getReference(view: View){
        profRatingBar = view.findViewById(R.id.ratingBar_new_rating_id)
        userCommentEditText = view.findViewById(R.id.editText_user_comment)
        saveReviewBtn = view.findViewById(R.id.save_review_btn_id)
        cancelReviewBtn = view.findViewById(R.id.cancel_review_btn_id)
        deleteReviewBtn = view.findViewById(R.id.delete_review_btn_id)
    }

    private fun onClickButtonHandler(view: View) {
        saveReviewBtn.setOnClickListener {
        // check if comment is entered. Default of rating is 0
            if(userCommentEditText.text.isNullOrEmpty()){
                if(isAdded){
                    Toast.makeText(requireActivity(), "The comment cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                // save the entries to the database
                    // then go back to displaying reviews
                val selectedProfName = arguments?.getString("PROF_NAME")

                // add the review to the Professor object in db
                dbReference.child("Professors/$selectedProfName").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.child("Reviews").hasChild(currUserId!!)) {
                                // the user has already entered a review for selectedProfName

                                val previousRating: Float = dataSnapshot.child("Reviews").child(currUserId).child("Rate").value.toString().toFloat()

                                // update previous review
                                dataSnapshot.child("Reviews").child(currUserId).child("Rate").ref.setValue(profRatingBar.rating)
                                dataSnapshot.child("Reviews").child(currUserId).child("Comment").ref.setValue(userCommentEditText.text.toString())

                                // update rating Sum
                                var reviewSum = dataSnapshot.child("Rating").child("Sum").value.toString().toFloat()
                                reviewSum -= previousRating
                                reviewSum += profRatingBar.rating
                                dataSnapshot.child("Rating").child("Sum").ref.setValue(reviewSum)
                            }
                            else {
                                // new review
                                    // Reviews
                                // add new review review
                                val review = dbReference.child("Professors/$selectedProfName/Reviews/$currUserId")
                                review.child("Rate").setValue(profRatingBar.rating)
                                review.child("Comment").setValue(userCommentEditText.text.toString())

                                    // Rating
                                // update rating count
                                var reviewCount = dataSnapshot.child("Rating").child("Count").value.toString().toInt()
                                reviewCount += 1
                                dataSnapshot.child("Rating").child("Count").ref.setValue(reviewCount)

                                // update rating Sum
                                var reviewSum = dataSnapshot.child("Rating").child("Sum").value.toString().toFloat()
                                reviewSum += profRatingBar.rating
                                dataSnapshot.child("Rating").child("Sum").ref.setValue(reviewSum)
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })

                // add the review to the User object in db
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
                                val review = dbReference.child("Users/${currUserId}/Reviews/$selectedProfName")
                                review.child("Rate").setValue(profRatingBar.rating)
                                review.child("Comment").setValue(userCommentEditText.text.toString())
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }

                    })


                if(isAdded) {
                    profListViewModel.currentFragment = 0
                    // changing parent activity's button
                    val rateReviewBtn =
                        requireActivity().findViewById<Button>(com.example.hubspot.R.id.rate_now_btn_id)
                    rateReviewBtn.text = "Rate Now!"

                    Toast.makeText(requireActivity(), "Review saved", Toast.LENGTH_SHORT).show()
                    // replacing the fragment to display reviews
                    val reviewListFragments = ReviewDisplayFragment()
                    val arg = Bundle()
                    arg.putString("PROF_NAME", selectedProfName)
                    reviewListFragments.arguments = arg
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_3_displays, reviewListFragments).commit()
                }
            }
        }
        cancelReviewBtn.setOnClickListener {
                // go back to displaying the reviews
            // changing parent activity's button
            if(isAdded) {
                profListViewModel.currentFragment = 0
                val rateReviewBtn =
                    requireActivity().findViewById<Button>(com.example.hubspot.R.id.rate_now_btn_id)
                rateReviewBtn.text = "Rate Now!"

                // replacing the fragment to display reviews
                val selectedProfName = arguments?.getString("PROF_NAME")
                val reviewListFragments = ReviewDisplayFragment()
                val arg = Bundle()
                arg.putString("PROF_NAME", selectedProfName)
                reviewListFragments.arguments = arg
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_3_displays, reviewListFragments).commit()

                Toast.makeText(requireActivity(), "Review cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        deleteReviewBtn.setOnClickListener {
            if(isAdded) {
                val selectedProfName = arguments?.getString("PROF_NAME")
                showAlertDialog(selectedProfName)
            }
        }
    }

    // load the previously entered review to the view
    private fun loadRating(dataSnapshot: DataSnapshot, selectedProfName: String){
        // user has already commented for selectedProfName
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

    // to display alert dialog
    private fun showAlertDialog(selectedProfName: String?){
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Delete review" )
            .setMessage("Are you sure you want to delete your review?")
            .setNegativeButton("No") {dialog, which ->
            }
            .setPositiveButton("Yes") {dialog, which ->
                changeFragment = true
                // Delete the review from the Professor object in db
                dbReference.child("Professors/$selectedProfName").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.child("Reviews").hasChild(currUserId!!)) {
                                // update Rating (count and sum)
                                val previousRating: Float = dataSnapshot.child("Reviews").child(currUserId).child("Rate").value.toString().toFloat()
                                    // count
                                var reviewCount = dataSnapshot.child("Rating").child("Count").value.toString().toInt()
                                reviewCount -= 1
                                dataSnapshot.child("Rating").child("Count").ref.setValue(reviewCount)
                                    // sum
                                var reviewSum = dataSnapshot.child("Rating").child("Sum").value.toString().toFloat()
                                reviewSum -= previousRating
                                dataSnapshot.child("Rating").child("Sum").ref.setValue(reviewSum)

                                // update Review (delete the review associated with userId)
                                dataSnapshot.child("Reviews").ref.child(currUserId!!).removeValue()

                            }
                        }
                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })
                // Delete the review from the User object in db
                dbReference.child("Users/${currUserId}/Reviews/").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.hasChild(selectedProfName!!)){
                                dataSnapshot.child(selectedProfName).ref.removeValue()
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })
                onResume()
            }
            .show()
    }
}