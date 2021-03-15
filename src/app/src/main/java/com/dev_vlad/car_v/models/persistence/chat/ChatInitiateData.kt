package com.dev_vlad.car_v.models.persistence.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatInitiateData(
    val carId: String,
    val carTitle: String,
    val ownerId: String,
    val featuredImgUrl: String,
    val dealerId: String,
    val initialOffer: Int,
    val initialOfferMsg: String,
    val offerId: String
) : Parcelable {

    override fun toString(): String {
        return "initiating chat data carId $carId title $carTitle ownerId $ownerId dealerId $dealerId initalOffer $initialOffer"
    }
}