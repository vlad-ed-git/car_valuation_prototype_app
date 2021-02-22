package com.dev_vlad.car_v.models.persistence.offers

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarOfferEntityDao {

    @Insert
    suspend fun insert(carOfferEntity: CarOfferEntity)

    @Query("DELETE FROM buy_offers WHERE dealerId=:dealerId AND carId=:carId")
    suspend fun deletePrevOffer(dealerId: String, carId: String)

    @Query("SELECT * FROM buy_offers WHERE dealerId=:dealerId AND carId=:carId")
    fun checkIfOfferWasMade(carId: String, dealerId: String) : Flow<List<CarOfferEntity>>
}