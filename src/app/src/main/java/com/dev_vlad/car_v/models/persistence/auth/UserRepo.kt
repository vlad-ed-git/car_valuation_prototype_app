package com.dev_vlad.car_v.models.persistence.auth

import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.view_models.auth.AuthViewModel
import kotlinx.coroutines.flow.Flow

class UserRepo
    (private val userEntityDao: UserEntityDao){


    companion object {
        private val TAG =  UserRepo::class.java.simpleName
    }

    val user: Flow<List<UserEntity>> = userEntityDao.getUser()

    suspend fun insertUser(
        user: UserEntity
    ){
        userEntityDao.deleteAll()
        userEntityDao.insert(user)
        MyLogger.logThis(
            TAG,
            "insertUser()",
            "savingUser -- $user"
        )
    }




}