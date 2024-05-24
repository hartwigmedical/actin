package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

data class ArcherVariantAnnotation(
    val evidence: ActionableEvidence? = null,
    val geneRole: GeneRole,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean? = null,
    val exonRank: Int? = null,
    val codonRank: Int? = null
)