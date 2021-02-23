package com.dev_vlad.car_v.view_models.sellers.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo

class ReceivedOffersViewModelFactory(
    private val repository: UserRepo,
    private val carRepo: CarRepo,
    private val offersRepo: OffersRepo
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceivedOffersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReceivedOffersViewModel(repository, carRepo, offersRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
