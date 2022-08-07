package com.example.hubspot.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hubspot.models.User

class FriendsViewModel: ViewModel() {
    var friendsList = MutableLiveData<ArrayList<User>>()
}