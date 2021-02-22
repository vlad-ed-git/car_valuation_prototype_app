package com.dev_vlad.car_v.view_models.activity_vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo

class MainActViewModelFactory(private val userRepo: UserRepo, private val carRepo: CarRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActViewModel(userRepo, carRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}