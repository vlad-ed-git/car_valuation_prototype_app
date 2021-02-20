package com.dev_vlad.car_v.view_models.sellers.add

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCarViewModel (private val userRepo: UserRepo, private val carRepo: CarRepo) : ViewModel() {

    init {
        setCurrentUser()
    }
    private lateinit var currentUser : UserEntity

    private fun setCurrentUser(){
        if (::currentUser.isInitialized)
            return
        viewModelScope.launch(Dispatchers.IO) {
            val userData = userRepo.getNonObservableUser()[0]
            withContext(Dispatchers.Main){
                currentUser = userData
            }
        }
    }

    private val savedCar = MutableLiveData<CarEntity>()
    fun getSavedCar() : LiveData<CarEntity> = savedCar

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
       val car = CarEntity(
           body_style = bodyStyle,
           make = make,
           model = model,
           year = year,
           color = color,
           condition = condition,
           mileage = mileage,
           extra_details = extraDetails,
           has_been_in_accident = hasBeenInAccident,
           has_flood_damage = hasFloodDamage,
           has_flame_damage = hasFlameDamage,
           has_issues_on_dashboard = hasIssuesOnDashboard,
           has_broken_or_replaced_odometer = hasBrokenOrReplacedOdometer,
           no_of_tires_to_replace = noOfTiresToReplace.toInt(),
           has_customizations = hasCustomizations,
           owner_id = currentUser.userId
       )
        viewModelScope.launch(Dispatchers.IO) {
           carRepo.addCar(car)
           withContext(Dispatchers.Main) {
               savedCar.value = car
               savingInProgress = false
           }
        }
    }

    var savingInProgress: Boolean = false
}