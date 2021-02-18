package com.dev_vlad.car_v.models.persistence.auth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dev_vlad.car_v.util.DEFAULT_USER_NAME
import com.dev_vlad.car_v.util.USER_TABLE_NAME

@Entity(tableName = USER_TABLE_NAME)
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    val userId : String,

    @ColumnInfo(name = "user_phone_number")
    val userPhone : String,

    @ColumnInfo(name = "user_location_country")
    val userLocationCountry : String,

    @ColumnInfo(name= "user_name")
    val userName: String = DEFAULT_USER_NAME,

    @ColumnInfo(name= "date_joined")
    val date_joined: Long,

    @ColumnInfo(name = "is_dealer")
    val is_dealer: Boolean = false,

    @ColumnInfo(name = "is_seller")
    val is_seller: Boolean = false
)