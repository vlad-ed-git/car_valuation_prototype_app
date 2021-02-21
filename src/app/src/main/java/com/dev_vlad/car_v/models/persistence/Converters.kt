package com.dev_vlad.car_v.models.persistence

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromString(string: String): List<String> {
        return string.split(",")
    }

    @TypeConverter
    fun listToString(stringList: List<String>): String {
        return stringList.joinToString(",")
    }
}