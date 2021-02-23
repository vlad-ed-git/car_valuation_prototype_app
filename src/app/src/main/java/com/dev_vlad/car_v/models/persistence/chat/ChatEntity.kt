package com.dev_vlad.car_v.models.persistence.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = false)
    var chatId: String,
    var ownerId: String,
    var dealerId: String,
    var carId: String,
    var message: String,
    var sentByOwner: Boolean,
    var sentByDealer: Boolean,
    var sentOn: Long
) {

    constructor() : this(
        chatId = "",
        ownerId = "",
        dealerId = "",
        carId = "",
        message = "",
        sentByOwner = false,
        sentByDealer = false,
        sentOn = 1L
    )
}


