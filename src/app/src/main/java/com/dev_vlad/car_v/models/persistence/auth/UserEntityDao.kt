package com.dev_vlad.car_v.models.persistence.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEntityDao {
    @Query("SELECT * FROM users_table ORDER BY date_joined DESC LIMIT 1")
    fun getUser(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity )

    @Query("DELETE FROM users_table")
    suspend fun deleteAll()
}