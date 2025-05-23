package com.hartwig.actin.datamodel.molecular.driver

object TestGeneAlterationFactory {

    fun createGeneAlteration(
        gene: String,
        geneRole: GeneRole = GeneRole.UNKNOWN,
        proteinEffect: ProteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance: Boolean? = null,
    ): GeneAlteration {
        return object : GeneAlteration {
            override val gene: String = gene
            override val geneRole: GeneRole = geneRole
            override val proteinEffect: ProteinEffect = proteinEffect
            override val isAssociatedWithDrugResistance: Boolean? = isAssociatedWithDrugResistance
        }
    }
}