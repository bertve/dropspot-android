package com.example.dropspot

import com.example.dropspot.data.model.Address
import com.example.dropspot.data.model.ParkCategory
import com.example.dropspot.data.model.SpotDetail
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SpotDetailTest {

    @Test
    fun safeSliderValueForEntranceFee_alreadySafeValue_TheSameValue() {
        val inputEntranceFee = 0.10000
        val expectedValue: Float = inputEntranceFee.toFloat()

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            0.0,
            0.0,
            Address("", "", "", "", "", ""),
            ArrayList(),
            false,
            false,
            ParkCategory.OUTDOOR_INDOOR,
            inputEntranceFee
        )

        assertThat(spotDetail.getSaveSliderValueForEntranceFee()).isEqualTo(expectedValue)
    }

    @Test
    fun safeSliderValueForEntranceFee_NotSafeValueA_SafeValue() {
        val inputEntranceFee = 0.10234
        val expectedValue: Float = 0.10.toFloat()

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            0.0,
            0.0,
            Address("", "", "", "", "", ""),
            ArrayList(),
            false,
            false,
            ParkCategory.OUTDOOR_INDOOR,
            inputEntranceFee
        )

        assertThat(spotDetail.getSaveSliderValueForEntranceFee()).isEqualTo(expectedValue)
    }

    @Test
    fun safeSliderValueForEntranceFee_NotSafeValueB_SafeValue() {
        val inputEntranceFee = 0.12345
        val expectedValue: Float = 0.10.toFloat()

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            0.0,
            0.0,
            Address("", "", "", "", "", ""),
            ArrayList(),
            false,
            false,
            ParkCategory.OUTDOOR_INDOOR,
            inputEntranceFee
        )

        assertThat(spotDetail.getSaveSliderValueForEntranceFee()).isEqualTo(expectedValue)
    }

    @Test
    fun safeSliderValueForEntranceFee_NotSafeValueC_SafeValue() {
        val inputEntranceFee = 0.13456
        val expectedValue: Float = 0.15.toFloat()

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            0.0,
            0.0,
            Address("", "", "", "", "", ""),
            ArrayList(),
            false,
            false,
            ParkCategory.OUTDOOR_INDOOR,
            inputEntranceFee
        )

        assertThat(spotDetail.getSaveSliderValueForEntranceFee()).isEqualTo(expectedValue)
    }

    @Test
    fun latLngToDMS_NW() {
        val inputLat = 43.068888
        val inputLng = -111.972656
        val expectedDMSString = "N43°4'7.997\"\nW111°58'21.562\""

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            inputLat,
            inputLng,
            null,
            ArrayList(),
            false,
            false,
            null,
            null
        )

        assertThat(spotDetail.getLocationString()).isEqualTo(expectedDMSString)
    }

    @Test
    fun latLngToDMS_NE() {
        val inputLat = 42.032974
        val inputLng = 74.707031
        val expectedDMSString = "N42°1'58.706\"\nE74°42'25.312\""

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            inputLat,
            inputLng,
            null,
            ArrayList(),
            false,
            false,
            null,
            null
        )

        assertThat(spotDetail.getLocationString()).isEqualTo(expectedDMSString)
    }

    @Test
    fun latLngToDMS_SE() {
        val inputLat = -27.683528
        val inputLng = 119.707031
        val expectedDMSString = "S27°41'0.701\"\nE119°42'25.312\""

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            inputLat,
            inputLng,
            null,
            ArrayList(),
            false,
            false,
            null,
            null
        )

        assertThat(spotDetail.getLocationString()).isEqualTo(expectedDMSString)
    }

    @Test
    fun latLngToDMS_SW() {
        val inputLat = -34.885931
        val inputLng = -64.863281
        val expectedDMSString = "S34°53'9.352\"\nW64°51'47.812\""

        val spotDetail = SpotDetail(
            0,
            "test",
            0,
            "test",
            inputLat,
            inputLng,
            null,
            ArrayList(),
            false,
            false,
            null,
            null
        )

        assertThat(spotDetail.getLocationString()).isEqualTo(expectedDMSString)
    }
}
