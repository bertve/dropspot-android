package com.example.dropspot.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dropspot.data.model.SpotDetail

@Dao
interface SpotDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spotDetail: SpotDetail)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spots: List<SpotDetail>)

    @Delete
    suspend fun delete(spotDetail: SpotDetail)

    @Query("SELECT * FROM spot_details WHERE spotId = :id")
    fun getSpotDetailById(id: Long): LiveData<SpotDetail>

    @Query("SELECT * FROM spot_details WHERE creatorId = :id")
    fun getSpotDetailByCreatorId(id: Long): LiveData<List<SpotDetail>>

    @Query("SELECT * FROM spot_details WHERE liked = 1")
    fun getSpotLikedSpotDetails(): LiveData<List<SpotDetail>>
}
