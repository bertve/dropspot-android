package com.example.dropspot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "spots")
class Spot(
    @PrimaryKey
    val id: Long,
    val creatorId: Long,
    var name: String = "unnamed",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Serializable {

    override fun toString(): String {
        return "Spot(spotId=$id, name=$name, latitude=$latitude, longitude=$longitude, creatorId=$creatorId)"
    }
}
