package com.example.hubspot.ratings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hubspot.R

/**
 * A simple [Fragment] subclass.
 * Use the [CommentsDisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommentsDisplayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comments_display, container, false)
    }
}