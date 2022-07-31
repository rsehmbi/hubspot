package com.example.hubspot.studybuddy.friendsMap.viewModel

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hubspot.studybuddy.friendsMap.services.FriendLocation
import com.example.hubspot.studybuddy.friendsMap.services.FriendsMapService

class FriendsMapViewModel : ViewModel(), ServiceConnection {
    private var myMessageHandler: MyMessageHandler = MyMessageHandler(Looper.getMainLooper())
    private val _friendsLocations = MutableLiveData<ArrayList<FriendLocation>>()
    val friendsLocations: LiveData<ArrayList<FriendLocation>>
        get() {
            return _friendsLocations
        }

    // this is called in onBind if TrackingService returns non-null
    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        val binder = iBinder as FriendsMapService.MyBinder
        binder.setmsgHandler(myMessageHandler)
    }

    // this is called when service is killed by OS
    override fun onServiceDisconnected(name: ComponentName) {}

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == FriendsMapService.ARRAY_LIST) {
                val bundle = msg.data
                _friendsLocations.value = bundle.getSerializable(FriendsMapService.FRIENDS_LOCATIONS_KEY) as ArrayList<FriendLocation>
            }
        }
    }
}