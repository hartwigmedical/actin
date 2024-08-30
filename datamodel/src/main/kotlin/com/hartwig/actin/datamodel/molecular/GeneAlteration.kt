package com.hartwig.actin.datamodel.molecular

interface GeneAlteration {
    val gene: String
    val geneRole: GeneRole
    val proteinEffect: ProteinEffect
    val isAssociatedWithDrugResistance: Boolean?
}
