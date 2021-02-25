package com.dev_vlad.car_v.view_models.dealers.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo

class SentOffersViewModelFactory(private val userRepo: UserRepo, private val carRepo: CarRepo, private val offerRepo: OffersRepo) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SentOffersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SentOffersViewModel(userRepo = userRepo, carRepo = carRepo, offersRepo=offerRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}