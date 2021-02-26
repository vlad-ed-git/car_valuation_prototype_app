package com.dev_vlad.car_v.models.persistence.offers

import com.dev_vlad.car_v.util.*
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OffersRepo(
    private val carOfferEntityDao: CarOfferEntityDao
) {
    companion object {
        private val TAG = OffersRepo::class.java.simpleName
        private const val PAGE_SIZE = 30
    }

    private val registeredListeners = ArrayList<ListenerRegistration>()

    suspend fun addOffer(carOfferEntity: CarOfferEntity): Boolean {
        val offerId = addOfferToFireStore(carOfferEntity) ?: return false
        carOfferEntity.offerId = offerId
        carOfferEntityDao.deletePrevOffer(carOfferEntity.dealerId, carOfferEntity.carId)
        carOfferEntityDao.insert(carOfferEntity)
        return true
    }

    suspend fun getMyNonObservableOfferIfExist(
        carId: String,
        ownerId: String,
        dealersId: String
    ): CarOfferEntity? {
        val offerEntity = carOfferEntityDao.getOfferIfWasMade(carId, ownerId, dealersId)
        return if (offerEntity.isEmpty()) null
        else offerEntity[0] //should be only 1 anyways
    }

    private var loadReceivedOffersExhausted = false
    fun getReceivedOffers(userId: String, pageNo: Int): Flow<List<CarOfferEntity>> {
        loadReceivedOffersExhausted = false
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            PAGE_SIZE * (page - 1)
        }
        val totalPostsToLoad  = RECYCLER_PAGE_SIZE + offset
        return carOfferEntityDao.getReceivedOffers(userId, limit = totalPostsToLoad)
            .map {
                MyLogger.logThis(TAG, "getReceivedOffers()", "page : $pageNo userId $userId load $totalPostsToLoad == found ${it.size}")
                if (it.size < totalPostsToLoad && !loadReceivedOffersExhausted) {
                    loadReceivedOffersExhausted = true
                    loadReceivedOffersFromServer(sellerId = userId, pageNo = page)
                }
                it
            }

    }

    private var loadSentOffersExhausted = false
    fun getSentOffers(userId: String, pageNo: Int): Flow<List<CarOfferEntity>> {
        loadSentOffersExhausted = false
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            PAGE_SIZE * (page - 1)
        }

        val totalPostsToLoad  = RECYCLER_PAGE_SIZE + offset
        return carOfferEntityDao.getSentOffers(userId, limit = PAGE_SIZE)
                .map {
                    MyLogger.logThis(TAG, "getSentOffers()", "page : $pageNo userId $userId load $totalPostsToLoad found ${it.size}")
                    if (it.isEmpty() && !loadSentOffersExhausted) {
                        loadSentOffersExhausted = true
                        loadOffersIMade(dealerId = userId, pageNo = page)
                    }
                    it
                }
    }

    
    /********************* FIRE STORE **************************/
    private suspend fun addOfferToFireStore(offerEntity: CarOfferEntity): String? {
        return try {
            MyLogger.logThis(
                    TAG, "addOfferToFireStore()", "saving a new offer $offerEntity"
            )
            val offerDoc =
                    if (offerEntity.offerId.isEmpty()) {
                        Firebase.firestore.collection(OFFERS_COLLECTION_NAME).document()
                                .also {
                                    offerEntity.offerId = it.id
                                }
                    } else {
                        Firebase.firestore.collection(OFFERS_COLLECTION_NAME)
                                .document(offerEntity.offerId)
                    }
            offerDoc.set(offerEntity)
                    .await()
            offerEntity.offerId
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "addOfferToFireStore() offer $offerEntity", "${e.message} --exc", e
            )
            null
        }


    }
    
    suspend fun loadOffersIMade(dealerId: String, pageNo: Int) : Boolean {
            return try {

                MyLogger.logThis(TAG, "loadOffersIMade($pageNo)", "called--")
                val offersCollection = Firebase.firestore.collection(OFFERS_COLLECTION_NAME)
                //first as in prev data depends on page
                val limit = if (pageNo <= 1) PAGE_SIZE else ((pageNo - 1) * PAGE_SIZE)
                val prevData = offersCollection
                        .whereEqualTo(DEALER_ID_FIELD ,dealerId)
                        .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .get()
                        .await()

                // Get the last visible document
                var offersSnapshots = prevData.documents
                if (pageNo > 1) {
                    val lastVisible = offersSnapshots[offersSnapshots.size - 1]
                    val nextData = offersCollection
                            .whereEqualTo(DEALER_ID_FIELD ,dealerId)
                            .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                            .startAfter(lastVisible)
                            .limit(PAGE_SIZE.toLong())
                            .get()
                            .await()
                    offersSnapshots = nextData.documents
                }

                var i = 0 //TODO remove ...for debug only
                for (offer in offersSnapshots) {
                    val aOffer = offer.toObject<CarOfferEntity>()
                    if (aOffer != null) {
                        i++
                        carOfferEntityDao.insert(aOffer)
                    }
                }

                MyLogger.logThis(TAG, "loadOffersIMade($pageNo)", "found $i cars")
                true
            } catch (e: Exception) {
                MyLogger.logThis(TAG, "loadOffersIMade($pageNo)", "exc ${e.message}", e)
                false
            }

    }

    suspend fun loadReceivedOffersFromServer(sellerId: String, pageNo: Int) : Boolean {
       return try {

            MyLogger.logThis(TAG, "loadReceivedOffersFromServer($pageNo)", "called--")
            val offersCollection = Firebase.firestore.collection(OFFERS_COLLECTION_NAME)
            //first as in prev data depends on page
            val limit = if (pageNo <= 1) PAGE_SIZE else ((pageNo - 1) * PAGE_SIZE)
            val prevData = offersCollection
                .whereEqualTo(OWNER_ID_FIELD, sellerId)
                .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            // Get the last visible document
            var offersSnapshots = prevData.documents
            if (pageNo > 1) {
                val lastVisible = offersSnapshots[offersSnapshots.size - 1]
                val nextData = offersCollection
                    .whereEqualTo(OWNER_ID_FIELD, sellerId)
                    .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(PAGE_SIZE.toLong())
                    .get()
                    .await()
                offersSnapshots = nextData.documents
            }

            var i = 0 //TODO remove ...for debug only
            for (offer in offersSnapshots) {
                val aOffer = offer.toObject<CarOfferEntity>()
                if (aOffer != null) {
                    i++
                    carOfferEntityDao.insert(aOffer)
                }
            }

            MyLogger.logThis(TAG, "loadReceivedOffersFromServer($pageNo)", "found $i cars")
            true
        } catch (e: Exception) {
            MyLogger.logThis(TAG, "loadReceivedOffersFromServer($pageNo)", "exc ${e.message}", e)
            false
        }
    }

  

}