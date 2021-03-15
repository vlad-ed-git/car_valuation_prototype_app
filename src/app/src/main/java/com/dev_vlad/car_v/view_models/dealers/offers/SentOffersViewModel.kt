package com.dev_vlad.car_v.view_models.dealers.offers

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.offers.CarOfferEntity
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo
import com.dev_vlad.car_v.util.RECYCLER_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SentOffersViewModel(
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

    /***************** AUTH ************************/
    private var loadedPage = MutableLiveData<Int>(1)

    fun observeSentOffers(): LiveData<List<CarNSentOfferWrapper>> = loadedPage.switchMap {
        refreshPosts(page = it).asLiveData()
    }

    private fun refreshPosts(page : Int): Flow<List<CarNSentOfferWrapper>> =
            if (currentUser.value == null) emptyFlow()
            else offersRepo.getSentOffers(
                    userId = currentUser.value!!.userId,
                    pageNo = page,
            ).map {
                //check if dealer has sent offer for this
                val sentOffersWrapped = ArrayList<CarNSentOfferWrapper>()
                for (offer in it) {
                    val car = carRepo.getNonObservableCarDetailsById(
                            carId = offer.carId
                    )
                    if (car != null) {
                        val item = CarNSentOfferWrapper(
                                offer = offer,
                                car = car
                        )
                        sentOffersWrapped.add(item)
                    }
                }
                sentOffersWrapped
            }

    var isLoading = false
    fun fetchMoreCars(totalItemsInListNow: Int) {
        if (!isLoading) {
            isLoading = true
            //calculate current page
            val itemsLoaded = if (totalItemsInListNow > 0) totalItemsInListNow else 1
            val currentPage = (itemsLoaded / RECYCLER_PAGE_SIZE).toInt()
            val nextPage = currentPage + 1
            loadedPage.value = nextPage
        }
    }


}

data class CarNSentOfferWrapper(
        val car: CarEntity,
        val offer: CarOfferEntity
)