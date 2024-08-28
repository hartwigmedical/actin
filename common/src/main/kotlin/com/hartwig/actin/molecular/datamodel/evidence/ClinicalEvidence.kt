package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.actin.Displayable

enum class Country(private val display: String) : Displayable {
    NETHERLANDS("Netherlands"),
    BELGIUM("Belgium"),
    GERMANY("Germany"),
    US("United States"),
    OTHER("Other");

    override fun display(): String {
        return display
    }
}

data class ApplicableCancerType(val cancerType: String, val excludedCancerTypes: Set<String>)

interface Evidence {
    val sourceEvent: String
    val applicableCancerType: ApplicableCancerType
    val isCategoryVariant: Boolean?
}

enum class EvidenceTier {
    I, II, III, V
}

enum class EvidenceLevel {
    A, B, C, D
}

data class EvidenceDirection(
    val hasPositiveResponse: Boolean = false,
    val hasBenefit: Boolean = false,
    val isResistant: Boolean = false,
    val isCertain: Boolean = false
)

data class TreatmentEvidence(
    val treatment: String,
    val evidenceLevel: EvidenceLevel,
    val onLabel: Boolean,
    val direction: EvidenceDirection,
    val date: String,
    val description: String,
    override val isCategoryVariant: Boolean?,
    override val sourceEvent: String,
    override val applicableCancerType: ApplicableCancerType
) : Evidence

data class ExternalTrial(
    val title: String,
    val countries: Set<Country>,
    val url: String,
    val nctId: String,
    override val isCategoryVariant: Boolean?,
    override val sourceEvent: String,
    override val applicableCancerType: ApplicableCancerType
) : Comparable<ExternalTrial>, Evidence {

    override fun compareTo(other: ExternalTrial): Int {
        return title.compareTo(other.title)
    }
}

data class ClinicalEvidence(
    val externalEligibleTrials: Set<ExternalTrial> = emptySet(),
    val treatmentEvidence: Set<TreatmentEvidence> = emptySet(),
) {
    operator fun plus(other: ClinicalEvidence): ClinicalEvidence {
        return ClinicalEvidence(
            externalEligibleTrials + other.externalEligibleTrials,
            treatmentEvidence + other.treatmentEvidence
        )
    }
}
