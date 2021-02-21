package com.dev_vlad.car_v.view_models.sellers.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.cars.CarRepo

class MyCarDetailsViewModelFactory(private val carRepo: CarRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyCarDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyCarDetailsViewModel(carRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}