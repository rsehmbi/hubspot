package com.example.hubspot.studybuddy.friendsMap.services

import com.google.android.gms.maps.model.LatLng

/**
 * A data class that represents the location of a friend
 */
data class FriendLocation(var friendId: String, var friendDisplayName: String?, var location: LatLng?)

