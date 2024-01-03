package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.FusionComparator

data class Fusion(
    val geneStart: String,
    val geneTranscriptStart: String,
    val fusedExonUp: Int,
    val geneEnd: String,
    val geneTranscriptEnd: String,
    val fusedExonDown: Int,
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence
) : Driver, Comparable<Fusion> {

    override fun compareTo(other: Fusion): Int {
        return FusionComparator().compare(this, other)
    }
}