package com.dev_vlad.car_v

import android.app.Application
import com.dev_vlad.car_v.models.persistence.LocalDatabase
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.chat.ChatRepo
import com.dev_vlad.car_v.models.persistence.offers.OffersRepo

class CarVApp : Application() {
    private val database by lazy { LocalDatabase.getDatabase(this) }
    val userRepo by lazy { UserRepo(database.userEntityDao()) }
    val carRepo by lazy { CarRepo(database.carEntityDao()) }
    val offerRepo by lazy { OffersRepo(database.carOfferEntityDao()) }
    val chatRepo by lazy { ChatRepo(database.chatDao()) }
}
