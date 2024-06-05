package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.FusionComparator

data class ExtendedFusion(
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
) : Fusion, Comparable<ExtendedFusion> {

    override fun compareTo(other: ExtendedFusion): Int {
        return FusionComparator().compare(this, other)
    }
}