package com.dev_vlad.car_v.view_models.welcome

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.util.DEFAULT_USER_NAME
import com.dev_vlad.car_v.util.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WelcomeViewModel(private val repository: UserRepo) : ViewModel() {

    companion object {
        private val TAG = WelcomeViewModel::class.java.simpleName
    }

    val errorOccurred = MutableLiveData<Boolean>(false)

    val userState: LiveData<List<UserEntity>> = repository.user.asLiveData()

    private var userEntity: UserEntity? = null

    fun updateUser(setAsBuyer: Boolean, setAsSeller: Boolean, userGivenName: String?) {
        MyLogger.logThis(
            TAG, "updateUser()", "params isBuyer $setAsBuyer isSeller $setAsSeller"
        )
        userEntity?.let {
            val newUserData = UserEntity(
                userId = it.userId,
                userLocationCountry = it.userLocationCountry,
                userCode = it.userCode,
                userPhone = it.userPhone,
                userName = userGivenName ?: DEFAULT_USER_NAME,
                dateJoined = it.dateJoined,
                isSeller = setAsSeller,
                isDealer = setAsBuyer
            )
            viewModelScope.launch(Dispatchers.IO) {
                val isUserUpdated = repository.updateUserInServer(user = newUserData)
                if (isUserUpdated) {
                    repository.updateUser(user = newUserData)
                } else {
                    errorOccurred.value = true
                }
            }
        }


    }

    fun setUserData(user: UserEntity) {
        userEntity = user
    }
}