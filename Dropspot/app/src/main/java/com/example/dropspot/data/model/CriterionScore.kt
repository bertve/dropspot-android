package com.example.dropspot.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class CriterionScore(
    val criterionId: Long,
    val criterionName: String,
    val description: String,
    val score: Double
) : Parcelable {

    fun getRatingBarScore(): Int {
        return score.roundToInt()
    }
}
