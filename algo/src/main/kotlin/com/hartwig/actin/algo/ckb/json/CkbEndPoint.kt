package com.hartwig.actin.algo.ckb.json

import com.fasterxml.jackson.annotation.JsonProperty

data class CkbEndPoint(
    val id: Int,
    val name: String,
    val definition: String,
    @JsonProperty("unit_of_measure") val unitOfMeasure: String
)
