package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

data class MolecularMatchDetails(
    val sourceDate: LocalDate,
    val sourceEvent: String,
    val sourceEvidenceType: EvidenceType,
    val sourceUrl: String?
)
