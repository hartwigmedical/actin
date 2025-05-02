package com.hartwig.actin.datamodel.molecular.evidence

enum class CancerTypeMatchApplicability {
    SPECIFIC_TYPE,
    ALL_TYPES,
    OTHER_TYPE
}

data class TreatmentEvidence(
    val treatment: String,
    val molecularMatch: MolecularMatchDetails,
    val applicableCancerType: CancerType,
    val cancerTypeMatchApplicability: CancerTypeMatchApplicability,
    val evidenceLevel: EvidenceLevel,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val evidenceDirection: EvidenceDirection,
    val evidenceYear: Int,
    val efficacyDescription: String
) {
    fun isOnLabel() = cancerTypeMatchApplicability == CancerTypeMatchApplicability.SPECIFIC_TYPE || cancerTypeMatchApplicability == CancerTypeMatchApplicability.ALL_TYPES
}
 
