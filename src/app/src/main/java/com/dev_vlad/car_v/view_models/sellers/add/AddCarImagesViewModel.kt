package com.dev_vlad.car_v.view_models.sellers.add

import androidx.lifecycle.ViewModel
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.util.MyLogger

class AddCarImagesViewModel (private val carRepo: CarRepo) : ViewModel() {
    companion object{
        private val TAG = AddCarImagesViewModel::class.java.simpleName
    }

    private lateinit var carId : String
    fun setCarId(carId: String) {
        this.carId  = carId
        MyLogger.logThis(
            TAG, "setCarId()", "carId $carId"
        )
    }
}