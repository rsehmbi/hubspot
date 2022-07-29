package com.example.hubspot.services

import android.app.*
import android.content.Intent
import android.os.*
import com.example.hubspot.studybuddy.FriendLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendsLocationsService : Service(){
    companion object {
        const val FRIENDS_LOCATIONS_KEY = "friendsLocationsKey"
        const val ARRAY_LIST = 1
    }
    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null
    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
    private var friendsLocations: ArrayList<FriendLocation> = ArrayList()

    // This is called as soon as the services starts
    override fun onCreate() {
        super.onCreate()
        myBinder = MyBinder()
        createFriendsLocationListeners(1)
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
            this@FriendsLocationsService.msgHandler = msgHandler
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

    private fun updateFriendsLocationsList(friendId: Long, newLocation: LatLng){
        val iterator = friendsLocations.iterator()
        while(iterator.hasNext()){
            val item = iterator.next()
            if (item.friendId == friendId) {
                iterator.remove()
            }
        }
        val updatedFriend = FriendLocation(friendId, newLocation)
        friendsLocations.add(updatedFriend)
    }

    private fun createFriendsLocationListeners(userId: Long) {
        dbReference.child("Users/${userId}/friends").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (friendId in dataSnapshot.children) {
                        val friend = FriendLocation(friendId.value as Long, null)
                        friendsLocations.add(friend)
                        createLocationListener(friendId.value as Long)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            })
    }

    private fun createLocationListener(userId: Long){
        dbReference.child("Users/${userId}/currentLocation").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newLat = dataSnapshot.child("latitude").value as Double
                    val newLng = dataSnapshot.child("longitude").value as Double
                    val newLocation = LatLng(newLat, newLng)
                    updateFriendsLocationsList(userId, newLocation)
                    if(msgHandler != null){
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