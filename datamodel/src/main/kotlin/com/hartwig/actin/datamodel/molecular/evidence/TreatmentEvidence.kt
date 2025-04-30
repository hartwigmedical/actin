package com.hartwig.actin.datamodel.molecular.evidence

enum class TumorMatch {
    SPECIFIC,
    AGNOSTIC,
    OFF
}

data class TreatmentEvidence(
    val treatment: String,
    val molecularMatch: MolecularMatchDetails,
    val applicableCancerType: CancerType,
    val tumorMatch: TumorMatch,
    val evidenceLevel: EvidenceLevel,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val evidenceDirection: EvidenceDirection,
    val evidenceYear: Int,
    val efficacyDescription: String
) {
    val isOnLabel = tumorMatch == TumorMatch.SPECIFIC || tumorMatch == TumorMatch.AGNOSTIC
}
 
