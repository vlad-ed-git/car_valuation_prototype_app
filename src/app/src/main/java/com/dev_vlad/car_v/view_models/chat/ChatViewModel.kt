package com.dev_vlad.car_v.view_models.chat

import androidx.lifecycle.ViewModel
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.chat.ChatInitiateData
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo

class ChatViewModel(userRepo: UserRepo, carRepo: CarRepo, offersRepo: OffersRepo) : ViewModel() {

    lateinit var chatInitiateData : ChatInitiateData
    fun initializedData(chatInitializationData: ChatInitiateData) {
        chatInitiateData = chatInitializationData
    }
}