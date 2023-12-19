package com.hartwig.actin.molecular.datamodel.driver

interface GeneAlteration {
    fun gene(): String
    fun geneRole(): GeneRole
    fun proteinEffect(): ProteinEffect

    @JvmField
    val isAssociatedWithDrugResistance: Boolean?
}
