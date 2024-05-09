package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherVariant(
    val gene: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val type: VariantType,
    val codingEffect: CodingEffect,
    val hgvsCodingImpact: String,
    val evidence: ActionableEvidence? = null,
    val geneRole: GeneRole? = null,
    val proteinEffect: ProteinEffect? = null,
    val isAssociatedWithDrugResistance: Boolean? = null,
    val exonRank: Int? = null
) : PanelEvent {

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene $hgvsCodingImpact"
    }
}