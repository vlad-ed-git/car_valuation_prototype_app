package com.dev_vlad.car_v.models.persistence.cars

import androidx.core.net.toUri
import com.bumptech.glide.RequestManager
import com.dev_vlad.car_v.util.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CarRepo(
    private val carEntityDao: CarEntityDao,
    private val glideRequestMgr: RequestManager
) {

    companion object {
        private val TAG = CarRepo::class.java.simpleName
    }


    /************** LOCAL DB ***********/
    private var loadAllCarsExhausted = false
    fun getAllCars(pageNo: Int = 1, query: String?): Flow<List<CarEntity>> {
        loadAllCarsExhausted = false
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            RECYCLER_PAGE_SIZE * (page - 1)
        }
        val totalPostsToLoad = RECYCLER_PAGE_SIZE + offset
        return if (query.isNullOrBlank()) {
            MyLogger.logThis(TAG, "getAllCars()", "page : $pageNo total $totalPostsToLoad")
            carEntityDao.getAllCars(limit =  totalPostsToLoad).map {
                if (!loadAllCarsExhausted) {
                    //fetch more 
                    loadMoreCarsFromServer(pageNo = pageNo)
                    loadAllCarsExhausted = true
                }
                it
            }
        } else {
            MyLogger.logThis(TAG, "getAllCars()", "page : $pageNo query $query total $totalPostsToLoad")
            carEntityDao.searchAllCarsByQuery(
                limit = totalPostsToLoad,
                queryParam = "%${query.replace(' ', '%')}%"
            ).map {
                if (!loadAllCarsExhausted) {
                    //fetch more 
                    searchAllCarsFromServer(pageNo = pageNo, query=query)
                    loadAllCarsExhausted = true
                }
                it
            }
        }
    }


    private var loadAllCarsByUserExhausted = false
    fun getAllCarsOfUser(pageNo: Int = 1, userId: String, query: String?): Flow<List<CarEntity>> {
        loadAllCarsByUserExhausted = false
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            RECYCLER_PAGE_SIZE * (page - 1)
        }
        val totalPostsToLoad  = RECYCLER_PAGE_SIZE + offset
        return if (query.isNullOrBlank()) {
            carEntityDao.getAllCarsOfUser(userId, limit = totalPostsToLoad)
                .map {
                    MyLogger.logThis(
                            TAG,
                            "getAllCarsOfUser()",
                            "page : $pageNo userId $userId found ${it.size}"
                    )
                    if (it.size < totalPostsToLoad && !loadAllCarsExhausted) {
                        loadAllCarsByUserExhausted = true
                        loadMyCarsFromServer(userId = userId, pageNo = page)
                    }
                    it
                }
        } else {
            carEntityDao.searchAllCarsOfUserByQuery(
                userId,
                limit = totalPostsToLoad,
                queryParam = "%${query.replace(' ', '%')}%"
            ).map {
                MyLogger.logThis(
                        TAG,
                        "getAllCarsOfUser()",
                        "page : $pageNo userId $userId query $query found ${it.size}"
                )
                if (it.size < totalPostsToLoad && !loadAllCarsExhausted) {
                    loadAllCarsByUserExhausted = true
                    searchMyCarsFromServer(userId = userId, pageNo = page, query = query)
                }
                it
            }
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
            for (photo in photosToUpload) {
                if (photo.startsWith("https")) {
                    uploadedPhotoUrls.add(photo)
                    continue
                }
                val imageName = System.currentTimeMillis().toString() + "_img"
                val fileRef = folderRef.child(imageName)
                val compressedImage = compressImage(glideRequestMgr, photo)
                val uploadTask = if (compressedImage == null) {
                    fileRef.putFile(photo.toUri())
                }
                else{
                    fileRef.putBytes(compressedImage)
                }

                val downloadUrl = uploadTask.await()
                        .storage
                        .downloadUrl
                        .await()
                        .toString()
                uploadedPhotoUrls.add(downloadUrl)
                MyLogger.logThis(TAG, "UploadImages()", "image added at ${photo.toUri().lastPathSegment}")

            }
            return uploadedPhotoUrls
        } catch (exception: java.lang.Exception) {
            MyLogger.logThis(TAG, "UploadImages()", "exception thrown $exception", exception)
            return null
        }
    }


    private suspend fun loadMoreCarsFromServer(pageNo: Int = 1) {
        try {

            MyLogger.logThis(TAG, "loadMoreCarsFromServer($pageNo)", "called--")
            val carsCollection = Firebase.firestore.collection(CARS_COLLECTION_NAME)
            //first as in prev data depends on page
            val limit = if (pageNo <= 1) RECYCLER_PAGE_SIZE else ((pageNo - 1) * RECYCLER_PAGE_SIZE)
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
                    .limit(RECYCLER_PAGE_SIZE.toLong())
                    .get()
                    .await()
                carSnapshots = nextData.documents
            }

            var i = 0 //TODO remove ...for debug only
            for (car in carSnapshots) {
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
    private suspend fun loadMyCarsFromServer(userId: String, pageNo: Int) {
        try {

            MyLogger.logThis(TAG, "loadMyCarsFromServer($pageNo)", "called--")
            val carsCollection = Firebase.firestore.collection(CARS_COLLECTION_NAME)
            //first as in prev data depends on page
            val limit = if (pageNo <= 1) RECYCLER_PAGE_SIZE else ((pageNo - 1) * RECYCLER_PAGE_SIZE)
            val prevData = carsCollection
                    .whereEqualTo(OWNER_ID_FIELD, userId)
                    .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()

            // Get the last visible document
            var carSnapshots = prevData.documents
            if (pageNo > 1) {
                val lastVisible = carSnapshots[carSnapshots.size - 1]
                val nextData = carsCollection
                        .whereEqualTo(OWNER_ID_FIELD, userId)
                        .orderBy(DEFAULT_SORT_FIELD, Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(RECYCLER_PAGE_SIZE.toLong())
                        .get()
                        .await()
                carSnapshots = nextData.documents
            }

            var i = 0 //TODO remove ...for debug only
            for (car in carSnapshots) {
                val aCar = car.toObject<CarEntity>()
                if (aCar != null) {
                    i++
                    carEntityDao.insert(aCar)
                }
            }

            MyLogger.logThis(TAG, "loadMyCarsFromServer($pageNo)", "found $i cars")
        } catch (e: Exception) {
            MyLogger.logThis(TAG, "loadMyCarsFromServer($pageNo)", "exc ${e.message}", e)
        }
    }

    private fun searchMyCarsFromServer(userId: String, pageNo: Int, query: String) : Boolean {
        //TODO searching in firebase - my cars
        return true
    }
    private fun searchAllCarsFromServer(pageNo: Int, query: String) : Boolean {
        //TODO searching in firebase - all cars
        return true
    }



}
