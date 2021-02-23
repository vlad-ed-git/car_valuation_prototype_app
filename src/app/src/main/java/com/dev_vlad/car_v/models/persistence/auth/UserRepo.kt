package com.dev_vlad.car_v.models.persistence.auth

import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.PHONE_NUMBER_FIELD
import com.dev_vlad.car_v.util.USERS_COLLECTION_NAME
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserRepo
(private val userEntityDao: UserEntityDao) {


    companion object {
        private val TAG = UserRepo::class.java.simpleName
    }

    val user: Flow<List<UserEntity>> = userEntityDao.getUser()
    suspend fun getNonObservableUser() = userEntityDao.getNonObservableUser()

    suspend fun insertUser(
            user: UserEntity
    ) {
        userEntityDao.deleteAll()
        userEntityDao.insert(user)
    }

    suspend fun updateUser(user: UserEntity) =
            userEntityDao.updateUser(user)


    /********* SERVER *******************/
    suspend fun saveUserInFireStore(user: UserEntity): Boolean {
        return try {
            Firebase.firestore.collection(USERS_COLLECTION_NAME).document(user.userPhone).set(user)
                    .await()
            true
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "saveUserInFireStore()", "${e.message} --exc", e
            )
            false
        }


    }

    suspend fun updateUserInServer(user: UserEntity): Boolean {
        return try {
            //re - write
            Firebase.firestore.collection(USERS_COLLECTION_NAME).document(user.userPhone).set(user)
                    .await()
            true
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "saveUserInFireStore()", "${e.message} --exc", e
            )
            false
        }
    }

    suspend fun getUserFromServerIfExists(userPhone: String): UserEntity? {
        try {
            val usersCollection = Firebase.firestore.collection(USERS_COLLECTION_NAME)
                    .whereEqualTo(PHONE_NUMBER_FIELD, userPhone)
                    .limit(1L)
                    .get()
                    .await()
            val users = usersCollection.documents
            return if (users.isEmpty()) null
            else {
                val user = users[0].toObject<UserEntity>()
                user
            }
        } catch (e: Exception) {
            MyLogger.logThis(TAG, "getUserFromServerIfExists", "exc ${e.message}", e)
            return null
        }
    }


}