package com.hartwig.actin.datamodel.molecular.driver

object TestVariantAlterationFactory {

    fun createVariantAlteration(
        gene: String,
        geneRole: GeneRole = GeneRole.UNKNOWN,
        proteinEffect: ProteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance: Boolean? = null,
        isCancerAssociatedVariant: Boolean = false
    ): VariantAlteration {
        return VariantAlteration(
            gene = gene,
            geneRole = geneRole,
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance,
            isCancerAssociatedVariant = isCancerAssociatedVariant
        )
    }
}