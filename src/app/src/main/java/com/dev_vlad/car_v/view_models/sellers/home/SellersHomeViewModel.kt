package com.dev_vlad.car_v.view_models.sellers.home

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SellersHomeViewModel(
    private val userRepo: UserRepo,
    private val carRepo: CarRepo
) : ViewModel() {
    init {
        setCurrentUser()
    }

    private val currentUser = MutableLiveData<UserEntity>()
    private fun setCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val userData = userRepo.getNonObservableUser()[0]
            withContext(Dispatchers.Main) {
                currentUser.value = userData
            }
        }
    }

    fun getCurrentUser(): LiveData<UserEntity> = currentUser

    /*************** POSTS AND QUERIES ***************/
    private val postsState = MutableLiveData<PostsStateModifiers>(PostsStateModifiers())

    fun observeMyCarsState(): LiveData<List<CarEntity>> = postsState.switchMap {
        refreshPosts(query = it.query, page = it.page).asLiveData()
    }

    private fun refreshPosts(query: String?, page: Int = 1): Flow<List<CarEntity>> =
        if (currentUser.value == null) emptyFlow()
        else carRepo.getAllCarsByUser(
            pageNo = page,
            userId = currentUser.value!!.userId,
            query = query
        )


    //TODO
    fun searchPosts(query: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            //modify query and page
            val newState = PostsStateModifiers(
                query = query,
                page = 1
            )
            postsState.postValue(newState)
        }
    }

    //TODO
    fun clearQuery() = searchPosts(null)

    companion object {
        private val TAG = SellersHomeViewModel::class.java.simpleName
    }

}

data class PostsStateModifiers(
    var query: String? = null,
    var page: Int = 1
)