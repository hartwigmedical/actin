package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariantDetails
import com.hartwig.actin.molecular.sort.driver.VariantComparator

data class Variant(
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val type: VariantType,
    val variantAlleleFrequency: Double? = null,
    val canonicalImpact: TranscriptImpact,
    val extendedVariantDetails: ExtendedVariantDetails? = null,
    val isHotspot: Boolean,
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
        return VariantComparator().compare(this, other)
    }

    constructor(
        chromosome: String,
        position: Int,
        ref: String,
        alt: String,
        type: VariantType,
        canonicalImpact: TranscriptImpact,
        otherImpacts: Set<TranscriptImpact> = emptySet(),
        extendedVariantDetails: ExtendedVariantDetails? = null,
        isHotspot: Boolean,
        isReportable: Boolean,
        event: String,
        driverLikelihood: DriverLikelihood?,
        evidence: ClinicalEvidence,
        gene: String,
        geneRole: GeneRole,
        proteinEffect: ProteinEffect,
        isAssociatedWithDrugResistance: Boolean?,
    ) : this(
        chromosome,
        position,
        ref,
        alt,
        type,
        canonicalImpact,
        extendedVariantDetails?.copy(otherImpacts = otherImpacts),
        isHotspot,
        isReportable,
        event,
        driverLikelihood,
        evidence,
        gene,
        geneRole,
        proteinEffect,
        isAssociatedWithDrugResistance
    )
}

