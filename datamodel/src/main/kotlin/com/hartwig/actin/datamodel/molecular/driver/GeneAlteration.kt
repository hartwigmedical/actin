package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect

interface GeneAlteration {
    val gene: String
    val geneRole: GeneRole
    val proteinEffect: ProteinEffect
    val isAssociatedWithDrugResistance: Boolean?
}
