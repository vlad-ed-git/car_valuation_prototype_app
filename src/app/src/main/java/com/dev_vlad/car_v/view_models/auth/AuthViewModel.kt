package com.dev_vlad.car_v.view_models.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.views.auth.LoginFragment
import kotlinx.coroutines.launch

class AuthViewModel (private val repository: UserRepo) : ViewModel() {

    companion object {
        private val TAG =  AuthViewModel::class.java.simpleName
    }

    val userState: LiveData<List<UserEntity>> = repository.user.asLiveData()


    private fun saveUser(user: UserEntity) = viewModelScope.launch {
        repository.insertUser(user)
        MyLogger.logThis(
            TAG,
            "saveUser()",
            "savingUser -- $user"
        )
    }
}