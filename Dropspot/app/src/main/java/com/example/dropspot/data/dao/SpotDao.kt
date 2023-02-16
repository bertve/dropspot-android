package com.example.dropspot.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dropspot.data.model.Spot

@Dao
interface SpotDao {
    // onconflict replace handles duplicate inserts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spot: Spot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spots: List<Spot>)

    @Delete
    suspend fun delete(spot: Spot)

    @Query("SELECT * FROM spots")
    fun getAllSpots(): LiveData<List<Spot>>

    @Query("SELECT * FROM spots WHERE id=:id")
    fun getSpotById(id: Long): LiveData<Spot>

    @Query("SELECT * FROM spots WHERE creatorId=:id")
    fun getSpotByCreatorId(id: Long): LiveData<List<Spot>>
}
