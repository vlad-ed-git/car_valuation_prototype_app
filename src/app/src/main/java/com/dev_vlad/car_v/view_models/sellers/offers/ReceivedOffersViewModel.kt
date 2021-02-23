package com.dev_vlad.car_v.view_models.sellers.offers

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

class ReceivedOffersViewModel(
        private val userRepo: UserRepo,
        private val carRepo: CarRepo,
        private val offersRepo: OffersRepo)
    : ViewModel() {

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

    /***************** AUTH ************************/
    private var currentPage = 1

    fun observeReceivedOffers(): LiveData<List<CarNReceivedOfferWrapper>> = refreshPosts().asLiveData()

    private fun refreshPosts(): Flow<List<CarNReceivedOfferWrapper>> =
            if (currentUser.value == null) emptyFlow()
            else offersRepo.getReceivedOffers(
                    userId = currentUser.value!!.userId,
                    pageNo = currentPage,
            ).map {
                //check if dealer has sent offer for this
                val receivedOffersWrapped = ArrayList<CarNReceivedOfferWrapper>()
                for (offer in it) {
                    val car = carRepo.getNonObservableCarDetailsById(
                            carId = offer.carId
                    )
                    if (car != null) {
                        val item = CarNReceivedOfferWrapper(
                                offer = offer,
                                car = car
                        )
                        receivedOffersWrapped.add(item)
                    }
                }
                receivedOffersWrapped
            }


}

data class CarNReceivedOfferWrapper(
        val car: CarEntity,
        val offer: CarOfferEntity
)