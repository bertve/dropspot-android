package com.example.dropspot

import com.example.dropspot.data.model.ParkCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ParkCategoryTest {

    private val outdoorString = "Outdoor"
    private val indoorString = "Indoor"
    private val outdoorIndoorString = "Outdoor & Indoor"
    private val wrongString = "wrongString"

    @Test
    fun parkCategoryFromString_outdoorString_outdoorEnum() {
        val input = outdoorString
        val expected = ParkCategory.OUTDOOR
        val output = ParkCategory.parkCategoryFromString(input)
        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun parkCategoryFromString_indoorString_indoorEnum() {
        val input = indoorString
        val expected = ParkCategory.INDOOR
        val output = ParkCategory.parkCategoryFromString(input)
        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun parkCategoryFromString_outdoorIndoorString_outdoorIndoorEnum() {
        val input = outdoorIndoorString
        val expected = ParkCategory.OUTDOOR_INDOOR
        val output = ParkCategory.parkCategoryFromString(input)
        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun parkCategoryFromString_wrongString_outdoorIndoorEnum() {
        val input = wrongString
        val expected = ParkCategory.OUTDOOR_INDOOR
        val output = ParkCategory.parkCategoryFromString(input)
        assertThat(output).isEqualTo(expected)
    }
}
