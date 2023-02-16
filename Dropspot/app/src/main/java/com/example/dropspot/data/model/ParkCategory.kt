package com.example.dropspot.data.model

import java.util.Locale

enum class ParkCategory {

    OUTDOOR {
        override fun toString(): String {
            return "Outdoor"
        }
    },
    INDOOR {
        override fun toString(): String {
            return "Indoor"
        }
    },
    OUTDOOR_INDOOR {
        override fun toString(): String {
            return "Outdoor & Indoor"
        }
    };

    companion object {
        fun parkCategoryFromString(parkCatString: String): ParkCategory {
            when (parkCatString.trim().toUpperCase(Locale.ROOT)) {
                INDOOR.toString().toUpperCase(Locale.ROOT) -> return INDOOR
                OUTDOOR.toString().toUpperCase(Locale.ROOT) -> return OUTDOOR
                OUTDOOR_INDOOR.toString().toUpperCase(Locale.ROOT) -> return OUTDOOR_INDOOR
                else -> return OUTDOOR_INDOOR
            }
        }
    }
}
