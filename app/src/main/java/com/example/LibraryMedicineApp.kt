package com.example

import android.app.Application
import com.example.data.DataRepository

class LibraryMedicineApp : Application() {
    lateinit var repository: DataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = DataRepository()
    }
}
