package com.dev_vlad.car_v.view_models.dealers.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailsViewModel(private val carRepo: CarRepo) : ViewModel() {

    private val carData = MutableLiveData<CarEntity?>()
    fun observeCar(): LiveData<CarEntity?> = carData
    fun getCarId(): String? = carData.value?.carId
    fun fetchCarDetails(carId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val car = carRepo.getNonObservableCarDetailsById(carId)
            withContext(Dispatchers.Main) {
                carData.value = car
            }
        }
    }
}
