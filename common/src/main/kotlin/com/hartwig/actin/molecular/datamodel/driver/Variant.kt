package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.interpreted.InterpretedVariant
import com.hartwig.actin.molecular.sort.driver.VariantComparator

data class Variant(
    val variantCopyNumber: Double,
    val totalCopyNumber: Double,
    val isBiallelic: Boolean,
    val phaseGroups: Set<Int>?,
    val otherImpacts: Set<TranscriptImpact>,
    override val chromosome: String,
    override val position: Int,
    override val ref: String,
    override val alt: String,
    override val type: VariantType,
    override val isHotspot: Boolean,
    override val clonalLikelihood: Double,
    override val canonicalImpact: TranscriptImpact,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
) : InterpretedVariant, Comparable<Variant> {

    override fun compareTo(other: Variant): Int {
        return VariantComparator().compare(this, other)
    }
}
