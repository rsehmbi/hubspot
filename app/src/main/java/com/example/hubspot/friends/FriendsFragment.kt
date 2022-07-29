package com.example.hubspot.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.hubspot.R
import com.example.hubspot.studybuddy.MockUser
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase


class FriendsFragment  : Fragment() {
    private lateinit var friendEmail: EditText
    private lateinit var addFriendButton: Button
    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        friendEmail = view.findViewById(R.id.friendSearchEmail)
        addFriendButton = view.findViewById(R.id.addFriendButton)
        addFriendButton.setOnClickListener {
            // Do some work here
            addNewUser()
        }

        addNewUser()

        return view
    }

    private fun searchUserById(){
        // Creates a chronological UID for each list item
    }

    private fun addNewUser(){
        val usersReference = dbReference.child("Users").child("3")
        val friends = arrayListOf<Int>(1, 2)
        val location = LatLng(0.0, 0.0)
        val user = MockUser("user3", "user3@gmail", friends, location)
        usersReference.setValue(user)
        println("debug: clicked")
    }
}