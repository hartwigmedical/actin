package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceLevel
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceTier

fun evidenceTier(driver: Driver): EvidenceTier {
    return when {
        driver.evidence.treatmentEvidence.any {
            it.onLabel && it.evidenceLevel in setOf(
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


