package com.hartwig.actin.datamodel.molecular.evidence


data class TreatmentEvidence(
    val treatment: String,
    val molecularMatch: MolecularMatchDetails,
    val cancerTypeMatch: CancerTypeMatchDetails,
    val evidenceLevel: EvidenceLevel,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val evidenceDirection: EvidenceDirection,
    val evidenceYear: Int,
    val efficacyDescription: String,
    val treatmentApproachesDrugClass: Set<String>,
    val treatmentApproachesTherapy: Set<String>
) {
    fun isOnLabel() = cancerTypeMatch.applicability.isOnLabel()
}
 
