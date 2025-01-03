package com.hartwig.actin.evidence

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails

enum class TumorMatch {
    ON_LABEL,
    OFF_LABEL
}

enum class VariantMatch {
    EXACT,
    CATEGORY,
    CATEGORY_OTHER
}

data class ScoringMatch(val tumorMatch: TumorMatch, val variantMatch: VariantMatch)

data class ScoringConfig(val levels: Map<ScoringMatch, Int>, val level: ScoringLevel)

data class ScoringLevel(val scoring: Map<EvidenceLevelDetails, Int>)

fun create() = ScoringConfig(
    mapOf(
        ScoringMatch(TumorMatch.ON_LABEL, VariantMatch.EXACT) to 20,
        ScoringMatch(TumorMatch.ON_LABEL, VariantMatch.CATEGORY) to 19,
        ScoringMatch(TumorMatch.ON_LABEL, VariantMatch.CATEGORY_OTHER) to 18,
        ScoringMatch(TumorMatch.OFF_LABEL, VariantMatch.EXACT) to 17,
        ScoringMatch(TumorMatch.OFF_LABEL, VariantMatch.CATEGORY) to 16,
        ScoringMatch(TumorMatch.OFF_LABEL, VariantMatch.CATEGORY_OTHER) to 15,
    ),
    ScoringLevel(
        mapOf(
            EvidenceLevelDetails.UNKNOWN to 0,
            EvidenceLevelDetails.FDA_APPROVED to 100,
            EvidenceLevelDetails.GUIDELINE to 95,
            EvidenceLevelDetails.CLINICAL_STUDY to 90,
            EvidenceLevelDetails.CASE_REPORTS_SERIES to 25,
            EvidenceLevelDetails.PRECLINICAL to 10
        )
    )
)
