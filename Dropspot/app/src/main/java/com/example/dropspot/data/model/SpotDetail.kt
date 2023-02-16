package com.example.dropspot.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.Currency
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

@Parcelize
@Entity(tableName = "spot_details")
data class SpotDetail(
    @PrimaryKey
    val spotId: Long,
    var spotName: String,
    val creatorId: Long,
    val creatorName: String,
    val latitude: Double,
    val longitude: Double,
    @Embedded
    var address: Address?,
    var criteriaScore: ArrayList<CriterionScore>,
    var liked: Boolean,
    var owner: Boolean,
    var parkCategory: ParkCategory?,
    var entranceFee: Double?
) : Parcelable {

    fun getLocationString(): String {
        return if (address == null) {
            latLngToDMS(latitude, longitude)
        } else {
            address!!.getAddressString()
        }
    }

    private fun latLngToDMS(lat: Double, lng: Double): String {
        var latString: String = if (lat >= 0) "N" else "S"
        var lngString: String = if (lng >= 0) "E" else "W"

        latString += calcDMS(lat)
        lngString += calcDMS(lng)

        return "$latString\n$lngString"
    }

    private fun calcDMS(doubleCoordinate: Double): String {
        val absValue = abs(doubleCoordinate)

        val d = floor(absValue)
        val m = floor((absValue - d) * 60)
        val s = round((absValue - d - m / 60) * 3600 * 1000) / 1000

        var resD: Any = d
        var resM: Any = m
        var resS: Any = s

        if (d.toInt() - d == 0.0) resD = d.toInt()
        if (m.toInt() - m == 0.0) resM = m.toInt()
        if (s.toInt() - s == 0.0) resS = s.toInt()

        return "$resD°$resM'$resS\""
    }

    fun isPark(): Boolean {
        return address != null
    }

    fun getDamageString(): String {
        return Currency.getInstance("EUR").symbol + String.format("%.2f", entranceFee)
    }

    fun getSaveSliderValueForEntranceFee(): Float {
        entranceFee?.let {
            val jump = 0.05
            var res: Double = Math.floor(entranceFee!! * 100) / 100

            val mod = res % jump

            if (mod == 0.00) {
                return res.toFloat()
            }

            if (mod <= 0.02) res -= mod else res += jump - mod

            return res.toFloat()
        }
        return 0.0.toFloat()
    }

    fun getOverallScore(): Int {
        var res = 0.0
        criteriaScore.forEach {
            res += it.score
        }

        return (res / criteriaScore.size).roundToInt()
    }
}
