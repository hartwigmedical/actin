package com.hartwig.actin.molecular.datamodel.driver

interface GeneAlteration {
    val gene: String
    val geneRole: GeneRole
    val proteinEffect: ProteinEffect
    val isAssociatedWithDrugResistance: Boolean?
}
