package com.dev_vlad.car_v.view_models.sellers.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.util.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddOrEditCarViewModel(private val userRepo: UserRepo, private val carRepo: CarRepo) :
    ViewModel() {

    init {
        setCurrentUser()
    }


    var savingInProgress: Boolean = false
    var isEditingCar: Boolean = false
    private lateinit var currentUser: UserEntity

    private fun setCurrentUser() {
        if (::currentUser.isInitialized)
            return
        viewModelScope.launch(Dispatchers.IO) {
            val userData = userRepo.getNonObservableUser()[0]
            withContext(Dispatchers.Main) {
                currentUser = userData
            }
        }
    }

    private val carBeingEditedOrCreated = MutableLiveData<CarEntity>()
    fun initCarForEditing(carId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val car = carRepo.getNonObservableCarDetailsById(carId)
            withContext(Dispatchers.Main) {
                car?.let {
                    isEditingCar = true
                    carBeingEditedOrCreated.value = it
                }
            }
        }
    }

    fun getCarBeingEdited(): LiveData<CarEntity> = carBeingEditedOrCreated

    fun saveCarInfo(
        bodyStyle: String,
        make: String,
        model: String,
        year: String,
        color: String,
        condition: String,
        mileage: String,
        extraDetails: String,
        hasBeenInAccident: Boolean,
        hasFloodDamage: Boolean,
        hasFlameDamage: Boolean,
        hasIssuesOnDashboard: Boolean,
        hasBrokenOrReplacedOdometer: Boolean,
        noOfTiresToReplace: String,
        hasCustomizations: Boolean
    ) {

        if (!isEditingCar) {
            addNewCar(
                bodyStyle,
                make,
                model,
                year,
                color,
                condition,
                mileage,
                extraDetails,
                hasBeenInAccident,
                hasFloodDamage,
                hasFlameDamage,
                hasIssuesOnDashboard,
                hasBrokenOrReplacedOdometer,
                noOfTiresToReplace,
                hasCustomizations
            )
        } else if (carBeingEditedOrCreated.value != null) {
            updateExistingCar(
                bodyStyle,
                make,
                model,
                year,
                color,
                condition,
                mileage,
                extraDetails,
                hasBeenInAccident,
                hasFloodDamage,
                hasFlameDamage,
                hasIssuesOnDashboard,
                hasBrokenOrReplacedOdometer,
                noOfTiresToReplace,
                hasCustomizations
            )
        } else {
            //bug
            //should not happen
            MyLogger.logThis(
                TAG,
                "saveCarInfo()",
                "bug | isEditingCar was true but the edited car is null"
            )
        }
    }


    private fun updateExistingCar(
        bodyStyle: String,
        make: String,
        model: String,
        year: String,
        color: String,
        condition: String,
        mileage: String,
        extraDetails: String,
        hasBeenInAccident: Boolean,
        hasFloodDamage: Boolean,
        hasFlameDamage: Boolean,
        hasIssuesOnDashboard: Boolean,
        hasBrokenOrReplacedOdometer: Boolean,
        noOfTiresToReplace: String,
        hasCustomizations: Boolean
    ) {
        val oldCar = carBeingEditedOrCreated.value!!
        val updatedCar = CarEntity(
            carId = oldCar.carId,
            bodyStyle = bodyStyle,
            make = make,
            model = model,
            year = year,
            color = color,
            condition = condition,
            mileage = mileage,
            extraDetails = extraDetails,
            hasBeenInAccident = hasBeenInAccident,
            hasFloodDamage = hasFloodDamage,
            hasFlameDamage = hasFlameDamage,
            hasIssuesOnDashboard = hasIssuesOnDashboard,
            hasBrokenOrReplacedOdometer = hasBrokenOrReplacedOdometer,
            noOfTiresToReplace = noOfTiresToReplace.toInt(),
            hasCustomizations = hasCustomizations,
            ownerId = oldCar.ownerId,
            createdAt = oldCar.createdAt,
            imageUrls = oldCar.imageUrls
        )
        viewModelScope.launch(Dispatchers.IO) {
            carRepo.updateCar(updatedCar)
            withContext(Dispatchers.Main) {
                carBeingEditedOrCreated.value = updatedCar
            }
        }
    }

    private fun addNewCar(
        bodyStyle: String,
        make: String,
        model: String,
        year: String,
        color: String,
        condition: String,
        mileage: String,
        extraDetails: String,
        hasBeenInAccident: Boolean,
        hasFloodDamage: Boolean,
        hasFlameDamage: Boolean,
        hasIssuesOnDashboard: Boolean,
        hasBrokenOrReplacedOdometer: Boolean,
        noOfTiresToReplace: String,
        hasCustomizations: Boolean
    ) {
        //get a new car
        val car = CarEntity(
            bodyStyle = bodyStyle,
            make = make,
            model = model,
            year = year,
            color = color,
            condition = condition,
            mileage = mileage,
            extraDetails = extraDetails,
            hasBeenInAccident = hasBeenInAccident,
            hasFloodDamage = hasFloodDamage,
            hasFlameDamage = hasFlameDamage,
            hasIssuesOnDashboard = hasIssuesOnDashboard,
            hasBrokenOrReplacedOdometer = hasBrokenOrReplacedOdometer,
            noOfTiresToReplace = noOfTiresToReplace.toInt(),
            hasCustomizations = hasCustomizations,
            ownerId = currentUser.userId,
            imageUrls = arrayListOf<String>()
        )
        viewModelScope.launch(Dispatchers.IO) {
            carRepo.addCar(car)
            withContext(Dispatchers.Main) {
                carBeingEditedOrCreated.value = car
            }
        }
    }

    companion object {
        private val TAG = AddOrEditCarViewModel::class.java.simpleName
    }
}