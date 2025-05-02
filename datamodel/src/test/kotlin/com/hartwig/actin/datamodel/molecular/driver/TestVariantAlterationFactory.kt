package com.hartwig.actin.datamodel.molecular.driver

object TestVariantAlterationFactory {

    fun createVariantAlteration(
        gene: String,
        geneRole: GeneRole = GeneRole.UNKNOWN,
        proteinEffect: ProteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance: Boolean? = null,
        isHotspot: Boolean = false
    ): VariantAlteration {
        return object : VariantAlteration {
            override val gene: String = gene
            override val geneRole: GeneRole = geneRole
            override val proteinEffect: ProteinEffect = proteinEffect
            override val isAssociatedWithDrugResistance: Boolean? = isAssociatedWithDrugResistance
            override val isHotspot: Boolean = isHotspot
        }
    }
}