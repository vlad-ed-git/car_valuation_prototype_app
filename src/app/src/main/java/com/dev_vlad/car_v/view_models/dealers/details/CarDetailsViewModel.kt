package com.dev_vlad.car_v.view_models.dealers.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.offers.CarOfferEntity
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo
import com.dev_vlad.car_v.util.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailsViewModel(private val userRepo: UserRepo, private val carRepo: CarRepo, private val offersRepo: OffersRepo) : ViewModel() {


    /** ////////////////// AUTHENTICATION ///////////////////////// */
    init {
        setCurrentUser()
    }
    private var currentUser : UserEntity? = null

    private fun setCurrentUser() {
        if (currentUser != null)
            return
        viewModelScope.launch(Dispatchers.IO) {
            val userData = userRepo.getNonObservableUser()[0]
            withContext(Dispatchers.Main) {
                currentUser = userData
            }
        }
    }
    /** ////////////////// AUTH END ///////////////////////// */

    private val carData = MutableLiveData<CarEntity?>()
    fun observeCar(): LiveData<CarEntity?> = carData
    fun fetchCarDetails(carId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val car = carRepo.getNonObservableCarDetailsById(carId)
            withContext(Dispatchers.Main) {
                carData.value = car
            }
        }
    }



    enum class SENDING_OFFER_STATE{
        IDLE,
        SENDING,
        SENT,
        ERROR
    }
    val errMsg : Int? = null
    private val sendingOfferState = MutableLiveData<SENDING_OFFER_STATE>(SENDING_OFFER_STATE.IDLE)
    fun observeInitialOfferState() : LiveData<SENDING_OFFER_STATE> = sendingOfferState
    fun isSendingOffer(): Boolean {
        return sendingOfferState.value == SENDING_OFFER_STATE.SENDING
    }

    fun saveOffer(initialOfferPrice: Int, message: String) {
        sendingOfferState.value = SENDING_OFFER_STATE.SENDING
        val car =  carData.value
        val userId = currentUser?.userId
        if (car != null && userId != null) {
            val newOffer = CarOfferEntity(
                offerId  = "offer_todo",
                dealerId = userId,
                ownerId = car.ownerId,
                carId = car.carId,
                updatedAt = System.currentTimeMillis(),
                offerPrice = initialOfferPrice,
                offerMessage = message
            )
            viewModelScope.launch(Dispatchers.IO) {
               val isSent =  offersRepo.addOffer(newOffer)
                if (isSent){
                    sendingOfferState.value = SENDING_OFFER_STATE.SENT
                }else{
                    sendingOfferState.value = SENDING_OFFER_STATE.ERROR
                }
            }
        }else{
            MyLogger.logThis(TAG, "saveOffer", "car is null!")
            sendingOfferState.value = SENDING_OFFER_STATE.ERROR
        }
    }

    companion object{
        private val TAG = CarDetailsViewModel::class.java.simpleName
    }
}
