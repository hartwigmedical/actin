package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.interpreted.InterpretedFusion
import com.hartwig.actin.molecular.sort.driver.FusionComparator

data class Fusion(
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
) : InterpretedFusion, Comparable<Fusion> {

    override fun compareTo(other: Fusion): Int {
        return FusionComparator().compare(this, other)
    }
}