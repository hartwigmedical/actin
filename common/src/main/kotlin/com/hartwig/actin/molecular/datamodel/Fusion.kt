package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusion
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.sort.driver.FusionComparator

data class Fusion(
    val geneStart: String,
    val geneEnd: String,
    val geneTranscriptStart: String,
    val geneTranscriptEnd: String,
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val extendedFusion: ExtendedFusion? = null,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
) : Driver, Comparable<Fusion> {
    override fun compareTo(other: Fusion): Int {
        return FusionComparator().compare(this, other)
    }

    fun extendedFusionOrThrow() = extendedFusion
        ?: throw IllegalStateException("Fusion is expected to have extended properties. Is this an orange-based molecular record?")
}