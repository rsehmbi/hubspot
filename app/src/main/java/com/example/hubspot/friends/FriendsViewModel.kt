package com.example.hubspot.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsViewModel: ViewModel() {
    var friendsList = MutableLiveData<ArrayList<String>>()
}