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
        var userId: String,

        @ColumnInfo(name = "user_phone_number")
        var userPhone: String,

        @ColumnInfo(name = "user_code")
        var userCode: String,

        @ColumnInfo(name = "user_location_country")
        var userLocationCountry: String,

        @ColumnInfo(name = "user_name")
        var userName: String = DEFAULT_USER_NAME,

        @ColumnInfo(name = "date_joined")
        var dateJoined: Long = System.currentTimeMillis(),

        @ColumnInfo(name = "date_updated")
        var dateUpdated: Long = System.currentTimeMillis(),

        @ColumnInfo(name = "is_dealer")
        var isDealer: Boolean,

        @ColumnInfo(name = "is_seller")
        var isSeller: Boolean
) {
    override fun toString(): String {
        return "id $userId , phoneNumber $userCode - $userPhone , userName $userName , country $userLocationCountry, isDealer $isDealer, isSeller $isSeller"
    }
}