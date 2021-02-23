package com.dev_vlad.car_v.models.persistence.offers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buy_offers")
data class CarOfferEntity(
    @PrimaryKey(autoGenerate = false)
    var offerId: String,
    var carId: String,

    //name matches const OFFERS_DEALER_FIELD
    var dealerId: String,
    //name matches const OWNER_ID_FIELD
    var ownerId: String,
    var updatedAt: Long,
    //name matches const DEFAULT_SORT_OFFERS_FIELD
    var offerPrice: Int,
    var offerMessage: String
) {
    //empty constructor for firebase
    constructor() : this(
        offerId = "",
        carId = "",
        dealerId = "",
        ownerId = "",
        updatedAt = System.currentTimeMillis(),
        offerPrice = 0,
        offerMessage = ""
    )

    override fun toString(): String {
        return "Offer on car $carId owned by $ownerId from $dealerId is $offerPrice"
    }
}