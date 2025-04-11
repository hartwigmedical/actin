package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

data class MolecularMatchDetails(
    val sourceDate: LocalDate,
    val sourceEvent: String,
    val isCategoryEvent: Boolean,
    val sourceEvidenceType: EvidenceType,
    val sourceUrls: Set<String>
)
