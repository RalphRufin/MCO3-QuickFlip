package com.mobdeve.s20.group7.mco2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobdeve.s20.group7.mco2.models.User

class UserViewModel : ViewModel() {
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }
}