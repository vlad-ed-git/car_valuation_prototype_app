package com.dev_vlad.car_v.view_models.dealers.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.cars.CarRepo

class CarDetailsViewModelFactory (private val carRepo: CarRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarDetailsViewModel(carRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}