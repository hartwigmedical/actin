package com.hartwig.actin.algo.ckb.datamodel

data class CkbDerivedMetric(
    val relativeMetricId: Int,
    val comparatorStatistic: String?,
    val comparatorStatisticType: String?,
    val confidenceInterval95Cs: String?,
    val pValue: String
)
