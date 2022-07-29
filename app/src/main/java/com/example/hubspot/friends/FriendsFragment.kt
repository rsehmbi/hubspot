package com.example.hubspot.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.hubspot.R
import com.google.firebase.database.FirebaseDatabase

class FriendsFragment  : Fragment() {
    private lateinit var friendEmail: EditText
    val usersReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference.child(
            "Courses"
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        friendEmail = view.findViewById(R.id.friendSearchEmail)



        return view
    }
}