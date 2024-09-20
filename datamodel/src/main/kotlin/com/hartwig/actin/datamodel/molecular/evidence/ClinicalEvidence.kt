package com.hartwig.actin.datamodel.molecular.evidence

import com.hartwig.actin.datamodel.Displayable
import com.hartwig.serve.datamodel.EvidenceLevelDetails
import java.time.LocalDate

data class Country(val name: CountryName, val hospitalsPerCity: Map<String, Set<String>>)

enum class CountryName(private val display: String) : Displayable {
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
    val evidenceLevelDetails: EvidenceLevelDetails
    val applicableCancerType: ApplicableCancerType
    val isCategoryEvent: Boolean
}

enum class EvidenceTier {
    I, II, III, IV
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
    val ckbEntryDate: LocalDate,
    val efficacyDescription: String,
    val efficacyDescriptionYear: Int,
    override val isCategoryEvent: Boolean,
    override val sourceEvent: String,
    override val evidenceLevelDetails: EvidenceLevelDetails,
    override val applicableCancerType: ApplicableCancerType
) : Evidence

data class ExternalTrial(
    val title: String,
    val countries: Set<Country>,
    val url: String,
    val nctId: String,
    override val isCategoryEvent: Boolean,
    override val sourceEvent: String,
    override val evidenceLevelDetails: EvidenceLevelDetails,
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
