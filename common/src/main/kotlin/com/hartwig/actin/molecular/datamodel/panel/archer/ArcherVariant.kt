package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherVariant(
    val gene: String,
    val hgvsCodingImpact: String,
    val evidence: ActionableEvidence?,
    val geneAlteration: GeneAlteration?
) : PanelEvent {

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene $hgvsCodingImpact"
    }
}