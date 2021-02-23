package com.dev_vlad.car_v.models.persistence.cars

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dev_vlad.car_v.util.UNSAVED_CAR_ID


@Entity(tableName = "cars")
data class CarEntity(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "carId")
        var carId: String = "${UNSAVED_CAR_ID}_${System.currentTimeMillis()}",

        var bodyStyle: String,

        var extraDetails: String,

        var year: String,

        var make: String,

        var model: String,

        var color: String,

        var condition: String,

        var mileage: String,

        var hasBeenInAccident: Boolean,

        var hasFloodDamage: Boolean,

        var hasFlameDamage: Boolean,

        var hasIssuesOnDashboard: Boolean,

        var hasBrokenOrReplacedOdometer: Boolean,

        var noOfTiresToReplace: Int,

        var hasCustomizations: Boolean,

        //MATCH DEFAULT_SORT_FIELD
        var updatedAt: Long = System.currentTimeMillis(),

        var createdAt: Long = System.currentTimeMillis(),

        //MATCH CAR_OWNER_FIELD
        var ownerId: String,

        var imageUrls: List<String>

) {
    //empty constructor for fire store
    constructor() : this(
            carId = "",
            bodyStyle = "",
            extraDetails = "",
            year = "",
            make = "",
            model = "",
            color = "",
            condition = "",
            mileage = "",
            hasBeenInAccident = false,
            hasFloodDamage = false,
            hasFlameDamage = false,
            hasIssuesOnDashboard = false,
            hasBrokenOrReplacedOdometer = false,
            noOfTiresToReplace = 0,
            hasCustomizations = false,
            updatedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            ownerId = "",
            imageUrls = arrayListOf<String>()
    )
}


