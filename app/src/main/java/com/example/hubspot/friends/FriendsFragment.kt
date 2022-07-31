package com.example.hubspot.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

// TODO: Extract the adding/retrieving friends form DB logic from this class into a Service
class FriendsFragment  : Fragment() {
    private lateinit var secretId: TextView
    private lateinit var friendId: EditText
    private lateinit var addFriendButton: Button
    private lateinit var shareButton: Button
    private lateinit var friendAddedToast: Toast
    private var friendsList = ArrayList<String>()
    private lateinit var friendsListView: ListView

    private val currUserId = Auth.getCurrentUser()!!.id
    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference

    private lateinit var friendsListViewModel: FriendsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        friendId = view.findViewById(R.id.friendSearchId)
        addFriendButton = view.findViewById(R.id.addFriendButton)
        shareButton = view.findViewById(R.id.share_button)
        secretId = view.findViewById(R.id.secret_id)
        secretId.text = currUserId
        friendAddedToast = Toast.makeText(
            requireActivity(), "Friend added!",
            Toast.LENGTH_SHORT
        )

        shareButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, currUserId)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent,"Share To:"))
        }
        addFriendButton.setOnClickListener {
            addFriend(friendId.text.toString())
        }

        // list view for the list of current user's friends
        friendsListView = view.findViewById(R.id.friends_listview)
        val friendsList = ArrayList<String>()
        val friendsListAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, friendsList)
        friendsListView.adapter = friendsListAdapter
        friendsListViewModel = ViewModelProvider(requireActivity())[FriendsViewModel::class.java]
        friendsListViewModel.friendsList.observe(requireActivity()) {
            friendsListAdapter.clear()
            friendsListAdapter.addAll(it)
            friendsListAdapter.notifyDataSetChanged()
        }
        getFriends()

        return view
    }

    private fun getFriends(){
        dbReference.child("Users/${currUserId}/friends").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // clear previous friends list value
                    friendsList.clear()
                    friendsListViewModel.friendsList.value = friendsList
                    // create a list of friends users and update the UI
                    for(friend in dataSnapshot.children) {
                        val friendUser = User(friend.value.toString())
                        friendsList.add(friendUser.id.toString())
                    }
                    for ((friendIndex, friendUserId) in friendsList.withIndex()){
                        // get info of each friend by their userId
                        getFriendInfo(friendUserId, friendIndex)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            })
    }

    private fun getFriendInfo(friendId: String, friendIndex: Int){
        dbReference.child("Users/${friendId}/displayName").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    friendsList[friendIndex] = dataSnapshot.value as String
                    friendsListViewModel.friendsList.value = friendsList
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            })
    }

    private fun getNumOfFriends(dataSnapshot: DataSnapshot, userId: String) : Int {
        val friends = dataSnapshot.child(userId).child("friends")
        return if (friends.value == null) {
            0
        } else {
            val friendsAsList = friends.value as List<*>
            friendsAsList.size
        }
    }

    private fun addFriend(friendUserId: String) {
        dbReference.child("Users").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currUserNumOfFriends = getNumOfFriends(dataSnapshot, currUserId!!)
                    val friendUserNumOfFriends = getNumOfFriends(dataSnapshot, friendUserId)
                    if (isAddFriendValid(dataSnapshot, friendUserId)){
                        // add friend user to current user's friend list
                        dbReference.child("Users/${currUserId}/friends/${currUserNumOfFriends}").setValue(friendUserId)
                        // add current user to the friend user's friend list
                        dbReference.child("Users/${friendUserId}/friends/${friendUserNumOfFriends}").setValue(currUserId)
                        friendAddedToast.show()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            })
    }

    private fun isAddFriendValid(dataSnapshot: DataSnapshot, friendUserId: String): Boolean {
        var friendIsAlreadyInFriendList = false
        val friendExists = dataSnapshot.child(friendUserId).exists()
        var friendIsCurrUser = false
        val friends = dataSnapshot.child(currUserId!!).child("friends")
        for(friend in friends.children){
            if (friend.value == friendUserId){
                friendIsAlreadyInFriendList = true
            }
        }
        if(!friendExists){
            Toast.makeText(
                requireActivity(), "User does not exist!",
                Toast.LENGTH_SHORT
            ).show()
        }
        if(friendIsAlreadyInFriendList) {
            friendAddedToast.cancel()
            Toast.makeText(
                requireActivity(), "You've already added this person to your friends list!",
                Toast.LENGTH_SHORT
            ).show()
        }
        if(currUserId == friendUserId) {
            friendIsCurrUser = true
            Toast.makeText(
                requireActivity(), "You cannot add yourself to your friends list!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return friendUserId != "" && friendExists && !friendIsAlreadyInFriendList && !friendIsCurrUser
    }
}