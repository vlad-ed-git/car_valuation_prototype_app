package com.dev_vlad.car_v

import android.app.Application
import com.dev_vlad.car_v.models.persistence.LocalDatabase
import com.dev_vlad.car_v.models.persistence.auth.UserRepo

class CarVApp : Application() {
    private val database by lazy { LocalDatabase.getDatabase(this) }
    val repository by lazy { UserRepo(database.userEntityDao()) }
}