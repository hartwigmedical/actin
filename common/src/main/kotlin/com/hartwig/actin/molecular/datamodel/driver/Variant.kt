package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.VariantComparator

data class Variant(
    val type: VariantType,
    val variantCopyNumber: Double,
    val totalCopyNumber: Double,
    val isBiallelic: Boolean,
    val isHotspot: Boolean,
    val clonalLikelihood: Double,
    val phaseGroups: Set<Int>?,
    val canonicalImpact: TranscriptImpact,
    val otherImpacts: Set<TranscriptImpact>,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
) : Driver, GeneAlteration, Comparable<Variant> {

    override fun compareTo(other: Variant): Int {
        return VariantComparator().compare(this, other)
    }
}
