package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.actin.Displayable
import com.hartwig.serve.datamodel.EvidenceLevel

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

enum class ActinEvidenceCategory {
    APPROVED, ON_LABEL_EXPERIMENTAL, OFF_LABEL_EXPERIMENTAL, PRE_CLINICAL, KNOWN_RESISTANT, SUSPECT_RESISTANT
}

data class ApplicableCancerType(val cancerType: String, val excludedCancerTypes: Set<String>)

interface Evidence {
    val sourceEvent: String
    val applicableCancerType: ApplicableCancerType
}

enum class EvidenceTier{
    I, II, III, V
}

data class TreatmentEvidence(
    val treatment: String,
    val evidenceLevel: EvidenceLevel,
    val category: ActinEvidenceCategory,
    override val sourceEvent: String,
    override val applicableCancerType: ApplicableCancerType
) : Evidence

data class ExternalTrial(
    val title: String,
    val countries: Set<Country>,
    val url: String,
    val nctId: String,
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

    fun approvedTreatments() = filter(ActinEvidenceCategory.APPROVED).toSet()
    fun onLabelExperimentalTreatments() =
        filter(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL).filter { it !in approvedTreatments() }.toSet()

    fun offLabelExperimentalTreatments() =
        filter(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
            .filter { it !in approvedTreatments() && it !in onLabelExperimentalTreatments() }.toSet()

    fun preClinicalTreatments() =
        filter(ActinEvidenceCategory.PRE_CLINICAL)
            .filter { it !in approvedTreatments() && it !in onLabelExperimentalTreatments() && it !in offLabelExperimentalTreatments() }
            .toSet()

    fun knownResistantTreatments() = filter(ActinEvidenceCategory.KNOWN_RESISTANT)
        .filter { treatmentsRelevantForResistance(it) }.toSet()

    fun suspectResistantTreatments() = filter(ActinEvidenceCategory.SUSPECT_RESISTANT)
        .filter { treatmentsRelevantForResistance(it) }
        .filter { it !in knownResistantTreatments() }.toSet()

    private fun treatmentsRelevantForResistance(it: String) =
        it in approvedTreatments() || it in onLabelExperimentalTreatments() || it in offLabelExperimentalTreatments()

    private fun filter(category: ActinEvidenceCategory) =
        treatmentEvidence.filter { it.category == category }.map { it.treatment }

    operator fun plus(other: ClinicalEvidence): ClinicalEvidence {
        return ClinicalEvidence(
            externalEligibleTrials + other.externalEligibleTrials,
            treatmentEvidence + other.treatmentEvidence
        )
    }
}
