package com.hartwig.actin.molecular.interpretation

class AggregatedEvidenceKeyTest {

    companion object {
        fun createAggregatedEvidenceKey(aggregatedEvidenceKey: String): AggregatedEvidenceKey {
            return AggregatedEvidenceKey(null, null, aggregatedEvidenceKey)
        }
    }
}