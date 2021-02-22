package com.dev_vlad.car_v.view_models.dealers.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo

class DealersHomeViewModelFactory (private val repository: UserRepo, private val carRepo: CarRepo) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealersHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DealersHomeViewModel(repository, carRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}