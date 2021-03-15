package com.dev_vlad.car_v.models.persistence.chat

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Update
    suspend fun update(vararg chats: ChatEntity)

    @Delete
    suspend fun delete(vararg chats: ChatEntity)

    @Query("SELECT * FROM chats WHERE ownerId =:ownerId AND carId =:carId AND dealerId =:dealerId  ORDER BY sentOn DESC LIMIT :limit")
    fun getMessages(
        carId: String,
        ownerId: String,
        dealerId: String,
        limit: Int
    ): Flow<List<ChatEntity>>
}