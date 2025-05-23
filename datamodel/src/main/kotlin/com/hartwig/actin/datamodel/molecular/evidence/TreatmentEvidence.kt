package com.hartwig.actin.datamodel.molecular.evidence


data class TreatmentEvidence(
    val treatment: String,
    val treatmentTypes: Set<String>,
    val molecularMatch: MolecularMatchDetails,
    val cancerTypeMatch: CancerTypeMatchDetails,
    val evidenceLevel: EvidenceLevel,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val evidenceDirection: EvidenceDirection,
    val evidenceYear: Int,
    val efficacyDescription: String
) {
    fun isOnLabel() = cancerTypeMatch.applicability.isOnLabel()
}
 
