package com.hartwig.actin.algo.ckb.json

data class CkbTherapyOfPopulation (
    val id: Int,
    val therapyName: String,
    val synonyms: String?,
    val therapyDescriptions: List<CkbTherapyDescription>,
    val createDate: String,
    val updateDate: String
)