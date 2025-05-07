package com.hartwig.actin.treatment

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails

enum class TumorMatch {
    PATIENT,
    ALL,
    ANY
}

enum class VariantMatch {
    EXACT,
    CATEGORY,
    FUNCTIONAL_EFFECT_MATCH
}

data class ScoringMatch(val tumorMatch: TumorMatch, val variantMatch: VariantMatch)

data class ScoringConfig(val categoryMatchLevels: Map<ScoringMatch, Int>, val approvalPhaseLevel: ScoringLevel)

data class ScoringLevel(val scoring: Map<EvidenceLevelDetails, Int>)

fun create() = ScoringConfig(
    mapOf(
        ScoringMatch(TumorMatch.PATIENT, VariantMatch.EXACT) to 20,
        ScoringMatch(TumorMatch.PATIENT, VariantMatch.CATEGORY) to 19,
        ScoringMatch(TumorMatch.PATIENT, VariantMatch.FUNCTIONAL_EFFECT_MATCH) to 18,
        ScoringMatch(TumorMatch.ALL, VariantMatch.EXACT) to 17,
        ScoringMatch(TumorMatch.ALL, VariantMatch.CATEGORY) to 16,
        ScoringMatch(TumorMatch.ALL, VariantMatch.FUNCTIONAL_EFFECT_MATCH) to 15,
        ScoringMatch(TumorMatch.ANY, VariantMatch.EXACT) to 14,
        ScoringMatch(TumorMatch.ANY, VariantMatch.CATEGORY) to 13,
        ScoringMatch(TumorMatch.ANY, VariantMatch.FUNCTIONAL_EFFECT_MATCH) to 12,
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
