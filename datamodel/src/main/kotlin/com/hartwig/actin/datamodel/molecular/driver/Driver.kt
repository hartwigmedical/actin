package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier

fun evidenceTier(driver: Driver): EvidenceTier {
    return when {
        driver.evidence.treatmentEvidence.any {
            it.isOnLabel && it.evidenceLevel in setOf(
                EvidenceLevel.A,
                EvidenceLevel.B
            ) && !it.molecularMatch.isCategoryEvent
        } -> EvidenceTier.I

        driver.evidence.treatmentEvidence.isNotEmpty() -> EvidenceTier.II

        else -> EvidenceTier.III
    }
}

interface Driver : Actionable {
    val isReportable: Boolean
    val event: String
    val driverLikelihood: DriverLikelihood?

    fun evidenceTier() = evidenceTier(this)
}


