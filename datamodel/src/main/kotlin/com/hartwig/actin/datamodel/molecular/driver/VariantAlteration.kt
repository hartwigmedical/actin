package com.hartwig.actin.datamodel.molecular.driver

data class VariantAlteration(
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
    val isHotspot: Boolean
) : GeneAlteration