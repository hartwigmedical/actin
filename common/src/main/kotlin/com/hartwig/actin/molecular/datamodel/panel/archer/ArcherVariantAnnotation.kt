package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

data class ArcherVariantAnnotation(
    val evidence: ActionableEvidence?,
    val geneRole: GeneRole,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
    val exonRank: Int?,
    val codonRank: Int?
)