package com.dev_vlad.car_v.models.persistence.auth

import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.USERS_COLLECTION_NAME
import com.dev_vlad.car_v.view_models.auth.AuthViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

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

    suspend fun saveUserInFireStore(user: UserEntity) : Boolean {
        return try {
            Firebase.firestore.collection( USERS_COLLECTION_NAME).document(user.userId).set(user)
                .await()
            true
        }catch (e : Exception ){
            MyLogger.logThis(
                TAG, "saveUserInFireStore()" , "${e.message} --exc", e
            )
            false
        }



    }


}