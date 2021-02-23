package com.dev_vlad.car_v.models.persistence.cars

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev_vlad.car_v.util.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CarRepo(
        private val carEntityDao: CarEntityDao
) {

    companion object {
        private val TAG = CarRepo::class.java.simpleName
    }

    private val registeredListeners = ArrayList<ListenerRegistration>()

    /************** LOCAL DB ***********/
    private var loadAllCarsExhausted = false
    fun getAllCars(pageNo: Int = 1, query: String?): Flow<List<CarEntity>> {
        loadAllCarsExhausted = false
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            PAGE_SIZE * (page - 1)
        }
        return if (query.isNullOrBlank()) {
            MyLogger.logThis(TAG, "getAllCars()", "page : $pageNo")
            carEntityDao.getAllCars(PAGE_SIZE, offset).map {
                if (it.isEmpty() || it.size < PAGE_SIZE && !loadAllCarsExhausted) {
                    //fetch more on time
                    loadMoreCarsFromServer(pageNo = pageNo)
                    loadAllCarsExhausted = true
                }
                it
            }
        } else {
            MyLogger.logThis(TAG, "getAllCars()", "page : $pageNo query $query")
            carEntityDao.searchAllCarsByQuery(limit = PAGE_SIZE, offset = offset, queryParam = "%${query.replace(' ', '%')}%")
        }
    }

    private var loadAllCarsByUserExhausted = false
    fun getAllCarsByUser(pageNo: Int = 1, userId: String, query: String?): Flow<List<CarEntity>> {
        loadAllCarsByUserExhausted = false
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            PAGE_SIZE * (page - 1)
        }
        return if (query.isNullOrBlank()) {
            MyLogger.logThis(TAG, "getAllCarsByUser()", "page : $pageNo userId $userId offset $offset")
            carEntityDao.getAllCarsOfUser(userId, limit = PAGE_SIZE, offset = offset)
                    .map {
                        if (it.isEmpty() && !loadAllCarsExhausted) {
                            loadAllCarsByUserExhausted = true
                            loadUserCarsFromServer(userId = userId)
                        }
                        it
                    }
        } else {
            MyLogger.logThis(
                    TAG,
                    "getAllCarsByUser()",
                    "page : $pageNo userId $userId query $query offset $offset"
            )
            carEntityDao.searchAllCarsOfUserByQuery(
                    userId,
                    limit = PAGE_SIZE,
                    offset = offset,
                    queryParam = "%${query.replace(' ', '%')}%"
            )
        }
    }

    suspend fun deleteTmpCarAndSaveCar(car: CarEntity, oldId: String) {
        MyLogger.logThis(TAG, "deleteTmpCarAndSaveCar()", "carId : ${car.carId} oldCar $oldId")
        carEntityDao.deleteCarById(oldId)
        carEntityDao.insert(car)
    }

    suspend fun getNonObservableCarDetailsById(carId: String): CarEntity? {
        MyLogger.logThis(TAG, "getNonObservableCarDetailsById()", "carId $carId")
        return carEntityDao.getCarByIdNotObserved(carId)
    }

    /********** STD CRUD *******/
    suspend fun addCar(car: CarEntity) {
        MyLogger.logThis(TAG, "addCar()", "car : $car")
        carEntityDao.insert(car)
    }

    suspend fun updateCar(updatedCar: CarEntity) {
        MyLogger.logThis(TAG, "updateCar()", "car : ${updatedCar.carId}")
        carEntityDao.updateCar(updatedCar)
    }

    suspend fun deleteCar(car: CarEntity): Boolean {
        MyLogger.logThis(TAG, "deleteCar()", "car : ${car.carId}")
        val deleted = deleteCarsOnFireStore(car)
        if (deleted)
            carEntityDao.deleteCar(car)
        return deleted
    }


    /******************************* FIRE STORE *************************************/
    private suspend fun deleteCarsOnFireStore(car: CarEntity): Boolean {
        return try {
            Firebase.firestore.collection(CARS_COLLECTION_NAME).document(car.carId)
                    .delete()
                    .await()
            true
        } catch (e: Exception) {
            MyLogger.logThis(TAG, "deleteCarsOnFireStore()", "deleting failed ${e.message}", e)
            false
        }


    }

    suspend fun addMyCarToFireStore(car: CarEntity): String? {
        return try {
            val carDoc =
                    if (car.carId.substring(0, UNSAVED_CAR_ID.length) == UNSAVED_CAR_ID) {
                        MyLogger.logThis(
                                TAG, "addMyCarToFireStore()", "saving an unsaved new car"
                        )
                        Firebase.firestore.collection(CARS_COLLECTION_NAME).document()
                                .also {
                                    car.carId = it.id
                                }
                    } else {
                        Firebase.firestore.collection(CARS_COLLECTION_NAME).document(car.carId)
                    }
            carDoc.set(car)
                    .await()
            car.carId
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "addMyCarToFireStore()", "${e.message} --exc", e
            )
            null
        }


    }


    suspend fun uploadImages(photosToUpload: List<String>, ownerId: String): List<String>? {
        try {
            val storageRef = Firebase.storage.reference
            val folderRef = storageRef.child(CARS_COLLECTION_NAME).child(ownerId)
            val uploadedPhotoUrls = ArrayList<String>()
            for (photos in photosToUpload) {
                if (photos.startsWith("https")) {
                    uploadedPhotoUrls.add(photos)
                    continue
                }
                val uri = photos.toUri()
                val fileRef = folderRef.child("${uri.lastPathSegment}")
                val uploadTask = fileRef.putFile(uri)
                val downloadUrl = uploadTask.await()
                        .storage
                        .downloadUrl
                        .await()
                        .toString()
                uploadedPhotoUrls.add(downloadUrl)
                MyLogger.logThis(TAG, "UploadImages()", "image added at ${uri.lastPathSegment}")

            }
            return uploadedPhotoUrls
        } catch (exception: java.lang.Exception) {
            MyLogger.logThis(TAG, "UploadImages()", "exception thrown $exception", exception)
            return null
        }
    }

    //fetch cars for dealers
    private fun searchCarsInServer(pageNo: Int = 1, query: String?) {

    }

    private suspend fun loadMoreCarsFromServer(pageNo: Int = 1) {
        try {

            MyLogger.logThis(TAG, "loadMoreCarsFromServer($pageNo)", "called--")
            val carsCollection = Firebase.firestore.collection(CARS_COLLECTION_NAME)
            //first as in prev data depends on page
            val limit = if (pageNo <= 1) PAGE_SIZE else ((pageNo - 1) * PAGE_SIZE)
            val prevData = carsCollection
                    .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()

            // Get the last visible document
            var carSnapshots = prevData.documents
            if (pageNo > 1) {
                val lastVisible = carSnapshots[carSnapshots.size - 1]
                val nextData = carsCollection
                        .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(PAGE_SIZE.toLong())
                        .get()
                        .await()
                carSnapshots = nextData.documents
            }

            var i = 0 //TODO remove ...for debug only
            for ((index, car) in carSnapshots.withIndex()) {
                val aCar = car.toObject<CarEntity>()
                if (aCar != null) {
                    i++
                    carEntityDao.insert(aCar)
                }
            }

            MyLogger.logThis(TAG, "loadMoreCarsFromServer($pageNo)", "found $i cars")
        } catch (e: Exception) {
            MyLogger.logThis(TAG, "loadMoreCarsFromServer($pageNo)", "exc ${e.message}", e)
        }
    }


    private suspend fun loadUserCarsFromServer(userId: String) {
        try {
            MyLogger.logThis(TAG, "loadUserCarsFromServer($userId)", "called--")
            val carsCollection = Firebase.firestore.collection(CARS_COLLECTION_NAME)
            val prevData = carsCollection
                    .whereEqualTo(CAR_OWNER_FIELD, userId)
                    .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                    .get()
                    .await()

            val carSnapshots = prevData.documents
            for (car in carSnapshots) {
                val aCar = car.toObject<CarEntity>()
                if (aCar != null) {
                    carEntityDao.insert(aCar)
                }
            }
        } catch (e: Exception) {
            MyLogger.logThis(TAG, "loadMoreCarsFromServer()", "exc ${e.message}", e)
        }
    }


    /*
    * TODO real time code listen to Cars on real time as they are posted by sellers
     */
    private val carsUpdates = MutableLiveData<CarDataStateWrapper>()
    fun getCarUpdates(): LiveData<CarDataStateWrapper> = carsUpdates
    fun listenForCarUpdates() {
        val carCollection = Firebase.firestore.collection(CARS_COLLECTION_NAME)
        val carUpdatesListener = carCollection.addSnapshotListener { snapshots, e ->
            if (e != null) {
                MyLogger.logThis(TAG, "listenForCarUpdates()", "exc ${e.message}", e)
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        MyLogger.logThis(TAG, "DocumentChange.Type.ADDED ", "New car: ${dc.document.data}")
                        val newCar = dc.document.toObject<CarEntity>()
                        carsUpdates.value = CarDataStateWrapper(
                                car = newCar,
                                state = DataState.ADD
                        )
                    }
                    DocumentChange.Type.MODIFIED -> {
                        MyLogger.logThis(TAG, "DocumentChange.Type.MODIFIED", "Modified city: ${dc.document.data}")
                        val modifiedCar = dc.document.toObject<CarEntity>()
                        carsUpdates.value = CarDataStateWrapper(
                                car = modifiedCar,
                                state = DataState.UPDATE
                        )
                    }
                    DocumentChange.Type.REMOVED -> {
                        MyLogger.logThis(TAG, "DocumentChange.Type.REMOVED", "Removed city: ${dc.document.data}")
                        val deletedCar = dc.document.toObject<CarEntity>()
                        carsUpdates.value = CarDataStateWrapper(
                                car = deletedCar,
                                state = DataState.UPDATE
                        )
                    }
                    else -> {
                    }
                }
            }
        }
        registeredListeners.add(carUpdatesListener)
    }

    fun removeListeners() {
        for (listener in registeredListeners) {
            listener.remove()
        }
    }

}

enum class DataState {
    DELETE,
    UPDATE,
    ADD
}

data class CarDataStateWrapper(val car: CarEntity, val state: DataState)