package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

data class TreatmentEvidence(
    val treatment: String,
    val molecularMatch: MolecularMatchDetails,
    val applicableCancerType: CancerType,
    val isOnLabel: Boolean,
    val evidenceLevel: EvidenceLevel,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val evidenceDirection: EvidenceDirection,
    val evidenceDate: LocalDate,
    val evidenceYear: Int,
    val efficacyDescription: String
)

