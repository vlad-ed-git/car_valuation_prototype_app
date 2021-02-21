package com.dev_vlad.car_v.models.persistence.cars

import com.dev_vlad.car_v.util.MyLogger
import kotlinx.coroutines.flow.Flow

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
        MyLogger.logThis(TAG, "getAllCarsByUser()", "page : $pageNo userId $userId")
        return if (query.isNullOrBlank())
            carEntityDao.getAllCarsOfUser(userId, limit, offset)
        else
            carEntityDao.searchAllCarsOfUserByQuery(userId, limit, offset, queryParam = "%${query.replace(' ', '%')}%")
    }

    suspend fun addCar(car: CarEntity) {
        MyLogger.logThis(TAG, "addCar()", "car : $car")
        carEntityDao.insert(car)
    }

    suspend fun deleteCar(car: CarEntity) {
        MyLogger.logThis(TAG, "deleteCar()", "car : $car")
        carEntityDao.deleteCar(car)
    }

    suspend fun updateCar(car: CarEntity) {
        MyLogger.logThis(TAG, "updateCar()", "car : $car")
        carEntityDao.updateCar(car)
    }

    suspend fun getNonObservableCarDetailsById(carId: String): CarEntity? {
        MyLogger.logThis(TAG, "getNonObservableCarDetailsById()", "carId $carId")
        return carEntityDao.getCarByIdNotObserved(carId)
    }

}