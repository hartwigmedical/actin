package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier

fun evidenceTier(driver: Driver): EvidenceTier {
    return when {
        driver.evidence.treatmentEvidence.any {
            it.isOnLabel() && it.evidenceLevel in setOf(
                EvidenceLevel.A,
                EvidenceLevel.B
            ) && !it.molecularMatch.sourceEvidenceType.isCategoryEvent()
        } -> EvidenceTier.I

        driver.evidence.treatmentEvidence.isNotEmpty() -> EvidenceTier.II

        else -> EvidenceTier.III
    }
}

interface Driver : Actionable {
    val isReportable: Boolean
    val driverLikelihood: DriverLikelihood?

    fun evidenceTier() = evidenceTier(this)

    fun eventDisplay(): String = when (this) {
        is Variant -> displayVariantEvent(event, sourceEvent)
        is CopyNumber -> displayCopyNumberEvent(
            gene,
            canonicalImpact.minCopies,
            canonicalImpact.maxCopies,
            canonicalImpact.type,
            otherImpacts
        )

        is Virus -> displayVirusEvent(event, integrations)
        else -> event
    }

    private fun displayVariantEvent(event: String, sourceEvent: String): String =
        if (event == sourceEvent) event else "$event (also known as $sourceEvent)"

    private fun displayCopyNumberEvent(
        gene: String,
        minCopies: Int?,
        maxCopies: Int?,
        canonicalImpactType: CopyNumberType,
        otherEffects: Set<TranscriptCopyNumberImpact>
    ): String =
        when (canonicalImpactType) {
            CopyNumberType.FULL_GAIN -> minCopies?.let { "$gene $minCopies copies" } ?: gene
            CopyNumberType.PARTIAL_GAIN -> maxCopies?.let { "$gene $maxCopies copies (partial)" } ?: "$gene (partial)"
            CopyNumberType.FULL_DEL, CopyNumberType.PARTIAL_DEL -> gene
            CopyNumberType.NONE -> if (otherEffects.all { it.type == CopyNumberType.NONE }) event else "$gene (alt transcript)"
        }

    private fun displayVirusEvent(event: String, integrations: Int?): String =
        event + (integrations?.let { " ($it integrations detected)" } ?: "")
}


