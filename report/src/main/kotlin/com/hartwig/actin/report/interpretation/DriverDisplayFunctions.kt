package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus

object DriverDisplayFunctions {

    fun Driver.eventDisplay(): String = when (this) {
        is Variant -> displayVariantEvent(event, sourceEvent)
        is CopyNumber -> {
            displayCopyNumberEvent(
                gene,
                event,
                canonicalImpact.minCopies,
                canonicalImpact.maxCopies,
                canonicalImpact.type,
                otherImpacts
            )
        }

        is Virus -> displayVirusEvent(event, integrations)
        else -> event
    }

    private fun displayVariantEvent(event: String, sourceEvent: String): String =
        if (event == sourceEvent) event else "$event (also known as $sourceEvent)"

    private fun displayCopyNumberEvent(
        gene: String,
        event: String,
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