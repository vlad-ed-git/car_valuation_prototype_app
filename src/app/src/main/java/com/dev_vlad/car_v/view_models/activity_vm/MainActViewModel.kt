package com.dev_vlad.car_v.view_models.activity_vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.view_models.auth.AuthViewModel

class MainActViewModel(private val userRepo: UserRepo, private val carRepo: CarRepo) : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    val userState: LiveData<List<UserEntity>> = userRepo.user.asLiveData()


    /********* DEALERS TODO OFFERS / CHATS
    /* sync */
    fun listenToCarUpdates(){
    carRepo.listenForCarUpdates()
    }

    fun getCarUpdates() = carRepo.getCarUpdates()


    fun addCar(car: CarEntity) {
    viewModelScope.launch(Dispatchers.IO) {
    carRepo.addCar(car)
    }
    }

    fun updateCar(car: CarEntity) {
    viewModelScope.launch(Dispatchers.IO) {
    carRepo.updateCar(car)
    }

    }

    fun deleteCar(car: CarEntity) {
    viewModelScope.launch(Dispatchers.IO) {
    carRepo.deleteCar(car)
    }
    }

     *************/


    override fun onCleared() {
        super.onCleared()
        carRepo.removeListeners()
    }


}