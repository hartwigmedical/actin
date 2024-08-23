package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceTier
import com.hartwig.serve.datamodel.EvidenceLevel

fun evidenceTier(driver: Driver): EvidenceTier {
    return when {
        driver.evidence.treatmentEvidence.any {
            it.category != ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL && it.evidenceLevel in setOf(
                EvidenceLevel.A,
                EvidenceLevel.B
            )
        } -> EvidenceTier.I

        driver.evidence.treatmentEvidence.isNotEmpty() -> EvidenceTier.II

        else -> EvidenceTier.III
    }
}

interface Driver {
    val isReportable: Boolean
    val event: String
    val driverLikelihood: DriverLikelihood?
    val evidence: ClinicalEvidence

    fun evidenceTier() = evidenceTier(this)
}


