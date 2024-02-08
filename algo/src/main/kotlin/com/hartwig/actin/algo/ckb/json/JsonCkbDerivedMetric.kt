package com.hartwig.actin.algo.ckb.json

import com.google.gson.annotations.SerializedName

data class JsonCkbDerivedMetric(
    @SerializedName("relative_metric_id") val relativeMetricId: Int,
    @SerializedName("comparator_statistic") val comparatorStatistic: String?,
    @SerializedName("comparator_statistic_type") val comparatorStatisticType: String?,
    @SerializedName("confidence_interval_95_cs") val confidenceInterval95Cs: String?,
    @SerializedName("p_value") val pValue: String
)
