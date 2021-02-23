package com.dev_vlad.car_v.view_models.dealers.home

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.offers.CarOfferEntity
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DealersHomeViewModel(
    private val userRepo: UserRepo,
    private val carRepo: CarRepo,
    private val offersRepo: OffersRepo
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

    fun observeCarsState(): LiveData<List<CarsWrapperForDealers>> = postsState.switchMap {
        refreshPosts(query = it.query, page = it.page).asLiveData()
    }

    private fun refreshPosts(query: String?, page: Int = 1): Flow<List<CarsWrapperForDealers>> =
        if (currentUser.value == null) emptyFlow()
        else carRepo.getAllCars(
            pageNo = page,
            query = query
        ).map {
            //check if dealer has sent offer for this
            val mappedCarsWrapper = ArrayList<CarsWrapperForDealers>()
            for (car in it) {
                val offer = offersRepo.getMyNonObservableOfferIfExist(
                    carId = car.carId,
                    ownerId = car.ownerId,
                    dealersId = currentUser.value!!.userId
                )
                val carsWrapper = CarsWrapperForDealers(
                    offerSent = offer,
                    car = car
                )
                mappedCarsWrapper.add(carsWrapper)
            }
            mappedCarsWrapper
        }


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
        private val TAG = DealersHomeViewModel::class.java.simpleName
    }

}

data class PostsStateModifiers(
    var query: String? = null,
    var page: Int = 1
)

data class CarsWrapperForDealers(
    val offerSent: CarOfferEntity?,
    val car: CarEntity
)