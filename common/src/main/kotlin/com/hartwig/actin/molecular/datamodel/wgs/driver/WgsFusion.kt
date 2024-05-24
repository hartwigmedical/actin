package com.hartwig.actin.molecular.datamodel.wgs.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.FusionComparator

data class WgsFusion(
    val geneTranscriptStart: String,
    val fusedExonUp: Int,
    val geneTranscriptEnd: String,
    val fusedExonDown: Int,
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
    override val geneStart: String,
    override val geneEnd: String,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence
) : Fusion, Comparable<WgsFusion> {

    override fun compareTo(other: WgsFusion): Int {
        return FusionComparator().compare(this, other)
    }
}