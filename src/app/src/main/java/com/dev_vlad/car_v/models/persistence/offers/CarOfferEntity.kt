package com.dev_vlad.car_v.models.persistence.offers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buy_offers")
data class CarOfferEntity(
    @PrimaryKey(autoGenerate = false)
    var offerId : String,
    var carId : String,
    var dealerId:String,
    var ownerId:String,
    var updatedAt : Long,
    var offerPrice : Int,
    var offerMessage: String
)