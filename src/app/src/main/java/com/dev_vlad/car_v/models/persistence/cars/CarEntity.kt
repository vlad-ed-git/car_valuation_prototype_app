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

        var body_style: String,

        var extra_details: String,

        var year: String,

        var make: String,

        var model: String,

        var color: String,

        var condition: String,

        var mileage: String,

        var has_been_in_accident: Boolean,

        var has_flood_damage: Boolean,

        var has_flame_damage: Boolean,

        var has_issues_on_dashboard: Boolean,

        var has_broken_or_replaced_odometer: Boolean,

        var no_of_tires_to_replace: Int,

        var has_customizations: Boolean,

        var updated_at: Long = System.currentTimeMillis(),

        var created_at: Long = System.currentTimeMillis(),

        var owner_id: String,

        var image_urls: List<String> = arrayListOf()

) : Parcelable
