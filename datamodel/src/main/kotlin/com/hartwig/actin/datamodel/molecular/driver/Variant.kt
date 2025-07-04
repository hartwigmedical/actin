package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class Variant(
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val type: VariantType,
    val variantAlleleFrequency: Double? = null,
    val canonicalImpact: TranscriptVariantImpact,
    val otherImpacts: Set<TranscriptVariantImpact>,
    val extendedVariantDetails: ExtendedVariantDetails? = null,
    val isCancerAssociatedVariant: Boolean,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
) : Driver, GeneAlteration, Comparable<Variant> {

    override fun compareTo(other: Variant): Int {
        return Comparator.comparing<Variant, Variant>({ it }, DriverComparator())
            .thenComparing({ it }, GeneAlterationComparator())
            .thenComparing({ it.canonicalImpact.hgvsProteinImpact }, String::compareTo)
            .thenComparing({ it.canonicalImpact.hgvsCodingImpact }, String::compareTo)
            .compare(this, other)
    }
}

