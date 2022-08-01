package com.example.hubspot.ratings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import com.example.hubspot.R


class AddNewReviewFragment : Fragment() {

    private lateinit var profRatingBar: RatingBar
    private lateinit var userCommentEditText: EditText
    private lateinit var saveReviewBtn: Button
    private lateinit var cancelReviewBtn: Button





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_add_new_review, container, false)

        // getting reference of all the views
        getReference(view)

        // handles clicks no save/cancel
        onClickButtonHandler()

        val selectedProfName = arguments?.getString("PROF_NAME")




        // check to see if the user has entered comment for this professor

            // if no: let the add a new review ==> only allow the to enter review when both rating and comment is entered

            // if yes: load the previously entered review from db and let the user edit it



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
            // save the entries in the database
                // then go back to displaying reviews
        }
        cancelReviewBtn.setOnClickListener {
            // go back to displaying the reviews
            val rateReviewBtn = requireActivity().findViewById<Button>(com.example.hubspot.R.id.rate_now_btn_id)
            rateReviewBtn.text = "Rate Now!"


            val selectedProfName = arguments?.getString("PROF_NAME")
            val reviewListFragments = ReviewDisplayFragment()
            val arg = Bundle()
            arg.putString("PROF_NAME", selectedProfName)
            reviewListFragments.arguments = arg
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_layout_3_displays,reviewListFragments).commit()
        }
    }



}