package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.sort.driver.FusionComparator

data class Fusion(
    val geneStart: String,
    val geneEnd: String,
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
    val extendedFusionDetails: ExtendedFusionDetails? = null,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
) : Driver, Comparable<Fusion> {

    override fun compareTo(other: Fusion): Int {
        return FusionComparator().compare(this, other)
    }

    fun extendedFusionOrThrow() = extendedFusionDetails
        ?: throw IllegalStateException("Fusion is expected to have extended properties. Is this an orange-based molecular record?")
}