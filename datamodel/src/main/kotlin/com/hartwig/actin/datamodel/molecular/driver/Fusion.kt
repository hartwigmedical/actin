package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.sort.driver.FusionComparator

data class Fusion(
    val geneStart: String,
    val geneEnd: String,
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
    val geneTranscriptStart: String?,
    val geneTranscriptEnd: String?,
    val fusedExonUp: Int?,
    val fusedExonDown: Int?,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
) : Driver, Comparable<Fusion> {

    override fun compareTo(other: Fusion): Int {
        return FusionComparator().compare(this, other)
    }
}