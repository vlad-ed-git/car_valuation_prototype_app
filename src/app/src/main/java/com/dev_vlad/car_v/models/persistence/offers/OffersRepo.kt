package com.dev_vlad.car_v.models.persistence.offers

import com.google.firebase.firestore.ListenerRegistration

class OffersRepo(
    private val carOfferEntityDao: CarOfferEntityDao
) {
    companion object {
        private val TAG = OffersRepo::class.java.simpleName
        private const val PAGE_SIZE = 30
    }

    private val registeredListeners = ArrayList<ListenerRegistration>()

    suspend fun addOffer(carOfferEntity : CarOfferEntity)  : Boolean{
        //todo fireStore first
        carOfferEntityDao.deletePrevOffer(carOfferEntity.dealerId, carOfferEntity.carId)
        carOfferEntityDao.insert(carOfferEntity)
        return true
    }

}