package com.example.dropspot.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dropspot.data.converters.Converters
import com.example.dropspot.data.dao.SpotDao
import com.example.dropspot.data.dao.SpotDetailDao
import com.example.dropspot.data.model.Spot
import com.example.dropspot.data.model.SpotDetail

@Database(entities = [Spot::class, SpotDetail::class], version = 25, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun spotDao(): SpotDao
    abstract fun spotDetailDao(): SpotDetailDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "spot_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
