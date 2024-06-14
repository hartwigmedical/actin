package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariant
import com.hartwig.actin.molecular.sort.driver.VariantComparator

data class Variant(
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val type: VariantType,
    val isHotspot: Boolean,
    val canonicalImpact: TranscriptImpact,
    val extendedVariant: ExtendedVariant? = null,
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

    fun extendedVariantOrThrow() = extendedVariant
        ?: throw IllegalStateException("Fusion is expected to have extended properties. Is this an orange-based molecular record?")
}

