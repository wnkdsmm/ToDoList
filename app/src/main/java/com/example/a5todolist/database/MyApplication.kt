package com.example.a5todolist.database

import android.app.Application
import android.content.Context
import androidx.room.Room

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeDatabase(applicationContext)
    }

    companion object {
        lateinit var database: AppDatabase
            private set

        fun isDatabaseInitialized(): Boolean {
            return this::database.isInitialized
        }

        fun initializeDatabase(context: Context) {
            database = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "task-database"
            ).build()
        }
    }
}
