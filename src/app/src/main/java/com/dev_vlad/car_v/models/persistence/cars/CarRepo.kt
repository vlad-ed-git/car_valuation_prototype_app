package com.dev_vlad.car_v.models.persistence.cars

import androidx.core.net.toUri
import com.dev_vlad.car_v.util.CARS_COLLECTION_NAME
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.UNSAVED_CAR_ID
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await

class CarRepo(
    private val carEntityDao: CarEntityDao
) {

    companion object {
        private val TAG = CarRepo::class.java.simpleName
        private const val PAGE_SIZE = 30
    }

    /************** LOCAL DB ***********/
    fun getAllCars(pageNo: Int = 1): Flow<List<CarEntity>> {
        val page = if (pageNo < 1) 1 else pageNo //safety
        val limit = PAGE_SIZE
        val offset = if (page == 1) {
            0
        } else {
            PAGE_SIZE * (page - 1)
        }
        MyLogger.logThis(TAG, "getAllCars()", "page : $pageNo")
        return carEntityDao.getAllCars(limit, offset)
    }

    fun getAllCarsByUser(pageNo: Int = 1, userId: String, query: String?): Flow<List<CarEntity>> {

        val page = if (pageNo < 1) 1 else pageNo //safety
        val limit = PAGE_SIZE
        val offset = if (page == 1) {
            0
        } else {
            PAGE_SIZE * (page - 1)
        }


        try {
            return if (query.isNullOrBlank()) {
                MyLogger.logThis(TAG, "getAllCarsByUser()", "page : $pageNo userId $userId offset $offset")
                  carEntityDao.getAllCarsOfUser(userId, limit, offset)
            } else {
                MyLogger.logThis(
                    TAG,
                    "getAllCarsByUser()",
                    "page : $pageNo userId $userId query $query offset $offset"
                )
                carEntityDao.searchAllCarsOfUserByQuery(
                    userId,
                    limit,
                    offset,
                    queryParam = "%${query.replace(' ', '%')}%"
                )
            }
        } catch (e: Exception) {
            MyLogger.logThis(
                TAG,
                "getAllCarsByUser()",
                "exc ${e.message}",
                e
            )
            return emptyFlow()
        }

    }

    suspend fun addCar(car: CarEntity) {
        MyLogger.logThis(TAG, "addCar()", "car : $car")
        carEntityDao.insert(car)
    }

    suspend fun deleteCar(car: CarEntity) {
        MyLogger.logThis(TAG, "deleteCar()", "car : $car")
        carEntityDao.deleteCar(car)
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


    suspend fun updateCar(updatedCar: CarEntity) {
        carEntityDao.updateCar(updatedCar)
    }



    /******************************* FIRE STORE *************************************/
    suspend fun addMyCarToFireStore(car: CarEntity): String? {
        return try {
            val carDoc = Firebase.firestore.collection(CARS_COLLECTION_NAME).document()
            if (car.carId.substring(0, UNSAVED_CAR_ID.length) == UNSAVED_CAR_ID) {
                car.carId = carDoc.id
                MyLogger.logThis(
                    TAG, "addMyCarToFireStore()", "saving an unsaved new car"
                )
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
                if(photos.startsWith("https")){
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


}