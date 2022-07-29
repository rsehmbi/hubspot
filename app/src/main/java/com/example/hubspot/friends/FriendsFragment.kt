package com.example.hubspot.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.hubspot.R
import com.example.hubspot.models.User
import com.example.hubspot.studybuddy.FriendsMapActivity
import com.google.firebase.database.FirebaseDatabase


class FriendsFragment  : Fragment() {
    private lateinit var friendEmail: EditText
    private lateinit var addFriendButton: Button
    private lateinit var friendsMapButton: Button
    private val dbInstance =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        friendEmail = view.findViewById(R.id.friendSearchEmail)
        addFriendButton = view.findViewById(R.id.addFriendButton)
        friendsMapButton = view.findViewById(R.id.friendsMapButton)
        addFriendButton.setOnClickListener {
            // Do some work here
            addNewUser()
        }

        friendsMapButton.setOnClickListener {
            val intent = Intent(requireActivity(), FriendsMapActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun searchUserById(){
        // Creates a chronological UID for each list item
    }

    private fun addNewUser(){
        val usersReference = dbInstance.reference.child("Users").child("3")
        val user = User("2222", "test@gmail")
        usersReference.setValue(user)
        println("debug: clicked")
    }
}