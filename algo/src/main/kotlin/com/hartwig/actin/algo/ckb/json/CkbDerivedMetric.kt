package com.hartwig.actin.algo.ckb.json

import com.fasterxml.jackson.annotation.JsonProperty

data class CkbDerivedMetric(
    @JsonProperty("relative_metric_id") val relativeMetricId: Int,
    @JsonProperty("comparator_statistic") val comparatorStatistic: String?,
    @JsonProperty("comparator_statistic_type") val comparatorStatisticType: String?,
    @JsonProperty("confidence_interval95_cs") val confidenceInterval95Cs: String?,
    @JsonProperty("p_value") val pValue: String
)
