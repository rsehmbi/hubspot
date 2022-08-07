package com.example.hubspot.studybuddy.friendsMap.services

import android.app.*
import android.content.Intent
import android.os.*
import com.example.hubspot.auth.Auth
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendsMapService : Service(){
    companion object {
        const val FRIENDS_LOCATIONS_KEY = "friendsLocationsKey"
        const val ARRAY_LIST = 1
    }
    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null
    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
    private var friendsLocations: ArrayList<FriendLocation> = ArrayList()
    private val currUserId = Auth.getCurrentUser()!!.id

    // This is called as soon as the services starts
    override fun onCreate() {
        super.onCreate()
        myBinder = MyBinder()
        createFriendsLocationListeners(currUserId!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // if service is killed by OS, don't restart it
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    inner class MyBinder : Binder() {
        // This can be called in the mapViewModel,
        // since the binder object is passed to the viewModel through onServiceConnected()
        fun setmsgHandler(msgHandler: Handler) {
            this@FriendsMapService.msgHandler = msgHandler
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        // if true we can bind/unbind repeatedly
        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private fun addFriendsLocationsList(friendId: String, displayName:String, newLocation: LatLng){
        val iterator = friendsLocations.iterator()
        while(iterator.hasNext()){
            val item = iterator.next()
            if (item.friendId == friendId) {
                iterator.remove()
            }
        }
        val updatedFriend = FriendLocation(friendId, displayName, newLocation)
        friendsLocations.add(updatedFriend)
    }

    private fun deleteFriendsLocationsList(friendId: String){
        val iterator = friendsLocations.iterator()
        while(iterator.hasNext()){
            val item = iterator.next()
            if (item.friendId == friendId) {
                iterator.remove()
            }
        }
    }

    private fun createFriendsLocationListeners(userId: String) {
        dbReference.child("Users/${userId}/friends").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (friendId in dataSnapshot.children) {
                        val friend = FriendLocation(friendId.value.toString(), null, null)
                        friendsLocations.add(friend)
                        createLocationListener(friendId.value.toString())
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            })
    }

    private fun createLocationListener(userId: String){
        dbReference.child("Users/${userId}").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.child("currentLocation").value != null && dataSnapshot.child("currentLocation/latitude").value != "none") {
                        val newLat =
                            dataSnapshot.child("currentLocation").child("latitude").value as Double
                        val newLng =
                            dataSnapshot.child("currentLocation").child("longitude").value as Double
                        val displayName = dataSnapshot.child("displayName").value.toString()
                        val newLocation = LatLng(newLat, newLng)
                        addFriendsLocationsList(userId, displayName, newLocation)
                    }
                    if (dataSnapshot.child("currentLocation/latitude").value == "none") {
                        deleteFriendsLocationsList(userId)
                    }
                    if (msgHandler != null) {
                        val bundle = Bundle()
                        bundle.putSerializable(FRIENDS_LOCATIONS_KEY, friendsLocations)
                        val message = msgHandler!!.obtainMessage()
                        message.data = bundle
                        message.what = ARRAY_LIST
                        msgHandler!!.sendMessage(message)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            }
        )
    }
}