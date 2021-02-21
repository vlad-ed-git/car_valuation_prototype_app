package com.dev_vlad.car_v.models.persistence.cars

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dev_vlad.car_v.util.UNSAVED_CAR_ID
import kotlinx.parcelize.Parcelize

@Entity(tableName = "cars")
@Parcelize
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

        var updatedAt: Long = System.currentTimeMillis(),

        var createdAt: Long = System.currentTimeMillis(),

        var ownerId: String,

        var imageUrls: List<String>

) : Parcelable
