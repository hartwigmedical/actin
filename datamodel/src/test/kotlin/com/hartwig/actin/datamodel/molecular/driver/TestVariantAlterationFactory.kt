package com.hartwig.actin.datamodel.molecular.driver

object TestVariantAlterationFactory {

    fun createMinimalVariantAlteration(
        gene: String
    ): VariantAlteration {
        return object : VariantAlteration {
            override val gene: String = gene
            override val geneRole: GeneRole = GeneRole.UNKNOWN
            override val proteinEffect: ProteinEffect = ProteinEffect.UNKNOWN
            override val isAssociatedWithDrugResistance: Boolean? = null
            override val isHotspot: Boolean = false
        }
    }

    fun createProperVariantAlteration(
        gene: String,
        geneRole: GeneRole,
        proteinEffect: ProteinEffect,
        isAssociatedWithDrugResistance: Boolean?,
        isHotspot: Boolean
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