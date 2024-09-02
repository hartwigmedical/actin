package com.hartwig.actin.datamodel.molecular.evidence

import com.hartwig.actin.datamodel.Displayable

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
    val approvalStatus: ApprovalStatus
    val applicableCancerType: ApplicableCancerType
    val isCategoryVariant: Boolean?
}

enum class EvidenceTier {
    I, II, III, IV
}

enum class EvidenceLevel {
    A, B, C, D
}

enum class ApprovalStatus(val display: String) {
    PRECLINICAL("Preclinical"),
    CASE_REPORTS_SERIES("Case Reports/Case Series"),
    PRECLINICAL_PATIENT_CELL_CULTURE("Preclinical - Patient cell culture"),
    PRECLINICAL_PDX_CELL_CULTURE("Preclinical - Pdx & cell culture"),
    PRECLINICAL_PDX("Preclinical - Pdx"),
    PRECLINICAL_CELL_CULTURE("Preclinical - Cell culture"),
    CLINICAL_STUDY("Clinical Study"),
    PRECLINICAL_CELL_LINE_XENOGRAFT("Preclinical - Cell line xenograft"),
    PRECLINICAL_BIOCHEMICAL("Preclinical - Biochemical"),
    PHASE_III("Phase III"),
    PHASE_II("Phase II"),
    PHASE_I("Phase I"),
    FDA_APPROVED_HAS_COMPANION_DIAGNOSTIC("FDA approved - Has Companion Diagnostic"),
    CLINICAL_STUDY_COHORT("Clinical Study - Cohort"),
    FDA_APPROVED("FDA approved"),
    GUIDELINE("Guideline"),
    FDA_APPROVED_ON_COMPANION_DIAGNOSTIC("FDA approved - On Companion Diagnostic"),
    PHASE_IB_II("Phase Ib/II"),
    PHASE_0("Phase 0"),
    CLINICAL_STUDY_META_ANALYSIS("Clinical Study - Meta-analysis"),
    FDA_CONTRAINDICATED("FDA contraindicated")
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
    override val isCategoryVariant: Boolean?,
    override val sourceEvent: String,
    override val approvalStatus: ApprovalStatus,
    override val applicableCancerType: ApplicableCancerType
) : Evidence

data class ExternalTrial(
    val title: String,
    val countries: Set<Country>,
    val url: String,
    val nctId: String,
    override val isCategoryVariant: Boolean?,
    override val sourceEvent: String,
    override val approvalStatus: ApprovalStatus,
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
