package com.dev_vlad.car_v.models.persistence.cars

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarEntityDao {

    @Query("SELECT * FROM cars ORDER BY updatedAt DESC LIMIT :limit")
    fun getAllCars(limit: Int): Flow<List<CarEntity>>

    @Query("SELECT * FROM cars WHERE ownerId =:userId ORDER BY updatedAt DESC LIMIT :limit")
    fun getAllCarsOfUser(userId: String, limit: Int): Flow<List<CarEntity>>

    @Query(
        "SELECT * FROM cars WHERE ownerId =:userId AND make LIKE :queryParam OR model LIKE :queryParam OR bodyStyle LIKE :queryParam OR year LIKE :queryParam OR color LIKE :queryParam ORDER BY updatedAt DESC LIMIT :limit"
    )
    fun searchAllCarsOfUserByQuery(
        userId: String,
        limit: Int,
        queryParam: String
    ): Flow<List<CarEntity>>

    @Query(
        "SELECT * FROM cars WHERE make LIKE :queryParam OR model LIKE :queryParam OR bodyStyle LIKE :queryParam OR year LIKE :queryParam OR color LIKE :queryParam ORDER BY updatedAt DESC LIMIT :limit"
    )
    fun searchAllCarsByQuery(limit: Int, queryParam: String): Flow<List<CarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(carEntity: CarEntity)

    @Delete
    suspend fun deleteCar(vararg cars: CarEntity)

    @Update
    suspend fun updateCar(cars: CarEntity)

    @Query("SELECT * FROM cars WHERE carId =:carId LIMIT 1")
    suspend fun getCarByIdNotObserved(carId: String): CarEntity?

    @Query("DELETE FROM cars WHERE carId =:oldId")
    fun deleteCarById(oldId: String)

}