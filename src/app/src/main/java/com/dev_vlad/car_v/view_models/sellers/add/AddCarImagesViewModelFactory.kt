package com.dev_vlad.car_v.view_models.sellers.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo

class AddCarImagesViewModelFactory (private val carRepo: CarRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCarImagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return  AddCarImagesViewModel(carRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}