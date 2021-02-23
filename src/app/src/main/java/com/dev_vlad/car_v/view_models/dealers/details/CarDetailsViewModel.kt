package com.dev_vlad.car_v.view_models.dealers.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.R
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


    //STATE
    enum class STATE {
        IDLE,
        INITIALIZING,
        INITIALIZED,
        SENDING,
        SENT,
        ERROR
    }

    var errMsg: Int? = null
    private val sendingOfferState = MutableLiveData<STATE>(STATE.IDLE)


    //OFFER DATA
    private var offerWrapper: OfferWrapper? = null
    fun getInitialOfferIfExist() = offerWrapper?.offer?.offerPrice
    fun getCarData() = offerWrapper!!.car
    fun fetchCarAndOfferDetails(carId: String) {
        sendingOfferState.value = STATE.INITIALIZING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dealer = userRepo.getNonObservableUser()[0]
                val car = carRepo.getNonObservableCarDetailsById(carId)
                val myOffer = offersRepo.getMyNonObservableOfferIfExist(carId = car!!.carId, ownerId = car.ownerId, dealersId = dealer.userId)
                withContext(Dispatchers.Main) {
                    offerWrapper = OfferWrapper(
                            car = car, dealer = dealer, offer = myOffer
                    )
                    sendingOfferState.value = STATE.INITIALIZED
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    MyLogger.logThis(
                            TAG, "fetchCarDetails()", "EXC RAISED ${exception.message}", exception)
                    errMsg = R.string.failed_to_init_car_details
                    offerWrapper = null
                    sendingOfferState.value = STATE.ERROR
                }
            }

        }
    }

    fun observeOfferState(): LiveData<STATE> = sendingOfferState
    fun isSendingOffer(): Boolean {
        return sendingOfferState.value == STATE.SENDING
    }

    fun saveOffer(initialOfferPrice: Int, message: String) {
        sendingOfferState.value = STATE.SENDING
        val car = offerWrapper?.car
        val userId = offerWrapper?.dealer?.userId
        val offerId = offerWrapper?.offer?.offerId ?: "" //TO BE SET DURING SAVE BY FIREBASE
        val prevPrice = offerWrapper?.offer?.offerPrice
        if (initialOfferPrice == prevPrice) {
            sendingOfferState.value = STATE.SENT
            return
        }
        if (car != null && userId != null) {
            val newOffer = CarOfferEntity(
                    offerId = offerId,
                    dealerId = userId,
                    ownerId = car.ownerId,
                    carId = car.carId,
                    updatedAt = System.currentTimeMillis(),
                    offerPrice = initialOfferPrice,
                    offerMessage = message
            )
            viewModelScope.launch(Dispatchers.IO) {
                val isSent = offersRepo.addOffer(newOffer)
                withContext(Dispatchers.Main) {
                    if (isSent) {
                        sendingOfferState.value = STATE.SENT
                    } else {
                        sendingOfferState.value = STATE.ERROR
                    }
                }
            }
        } else {
            MyLogger.logThis(TAG, "saveOffer", "car is null!")
            sendingOfferState.value = STATE.ERROR
        }
    }


    companion object {
        private val TAG = CarDetailsViewModel::class.java.simpleName
    }
}

data class OfferWrapper(
        val car: CarEntity,
        val offer: CarOfferEntity?,
        val dealer: UserEntity
)