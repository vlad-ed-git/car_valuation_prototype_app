package com.dev_vlad.car_v.models.persistence.offers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarOfferEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(carOfferEntity: CarOfferEntity)

    @Query("DELETE FROM buy_offers WHERE dealerId=:dealerId AND carId=:carId")
    suspend fun deletePrevOffer(dealerId: String, carId: String)


    @Query("SELECT * FROM buy_offers WHERE dealerId=:dealersId AND carId=:carId AND ownerId =:ownerId")
    suspend fun getOfferIfWasMade(
        carId: String,
        ownerId: String,
        dealersId: String
    ): List<CarOfferEntity>

    @Query("SELECT * FROM buy_offers WHERE ownerId =:userId ORDER BY offerPrice DESC LIMIT :limit OFFSET :offset")
    fun getReceivedOffers(userId: String, limit: Int, offset: Int): Flow<List<CarOfferEntity>>

}