package com.hartwig.actin.algo.ckb.json

import com.google.gson.annotations.SerializedName

data class CkbEndPoint(
    val id: Int,
    val name: String,
    val definition: String,
    @SerializedName("unit_of_measure") val unitOfMeasure: String
)
