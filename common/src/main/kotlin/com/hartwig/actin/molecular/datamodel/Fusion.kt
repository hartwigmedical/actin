package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusion
import com.hartwig.actin.molecular.sort.driver.FusionComparator

data class Fusion(
    val geneStart: String,
    val geneEnd: String,
    val extendedFusion: ExtendedFusion?,
    val geneTranscriptStart: String,
    val geneTranscriptEnd: String,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
) : Driver, Comparable<Fusion>{
    override fun compareTo(other: Fusion): Int {
        return FusionComparator().compare(this, other)
    }
}