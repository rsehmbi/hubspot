package com.example.hubspot.friends

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendsListAdapter(private val context: Context, private var friendsList: List<User>) : BaseAdapter(){
    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
    private val currUserId = Auth.getCurrentUser()!!.id

    override fun getItem(position: Int): Any {
        return friendsList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return friendsList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.listview_friends_layout,null)
        val friendName = view.findViewById(R.id.friend_name) as TextView
        val deleteButton = view.findViewById(R.id.delete_friend_button) as Button
        friendName.text = friendsList.get(position).displayName

        deleteButton.setOnClickListener {
            deleteFriend(friendsList.get(position).id.toString())
        }

        return view
    }

    fun replace(newManualEntryList: List<User>){
        this.friendsList = newManualEntryList
        notifyDataSetChanged()
    }

    private fun deleteFriend(friendUserId: String) {
        dbReference.child("Users/${currUserId}/friends/${friendUserId}").removeValue()
        dbReference.child("Users/${friendUserId}/friends/${currUserId}").removeValue()
    }
}