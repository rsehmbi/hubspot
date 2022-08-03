package com.example.hubspot.security.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * SecurityViewModel acts as a view model for the SecurityFragment in order to maintain an MVVM
 * architecture.
 */
class SecurityViewModel: ViewModel() {

    private val _getLocationNow = MutableLiveData<Boolean>().apply {
        value = false
    }

    val locationServiceSystemActivated = MutableLiveData<Boolean>()
    var getLocationNow = _getLocationNow
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val lastLocationDateTime = MutableLiveData<Long>()
    val silentButtonPressed = MutableLiveData<Boolean>()
    val keyEventButtonAction = MutableLiveData<Int>()
    val keyEventButtonKeyCode = MutableLiveData<Int>()
}