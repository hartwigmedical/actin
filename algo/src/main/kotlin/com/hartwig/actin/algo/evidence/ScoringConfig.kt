package com.hartwig.actin.algo.evidence

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

data class ScoringConfig(val categoryMatchLevels: Map<ScoringMatch, Int>, val approvalPhaseLevel: ScoringLevel)

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
            EvidenceLevelDetails.PHASE_III to 90,
            EvidenceLevelDetails.PHASE_II to 90,
            EvidenceLevelDetails.PHASE_IB_II to 40,
            EvidenceLevelDetails.PHASE_I to 35,
            EvidenceLevelDetails.PHASE_0 to 30,
            EvidenceLevelDetails.CLINICAL_STUDY to 30,
            EvidenceLevelDetails.CASE_REPORTS_SERIES to 25,
            EvidenceLevelDetails.PRECLINICAL to 10
        )
    )
)
