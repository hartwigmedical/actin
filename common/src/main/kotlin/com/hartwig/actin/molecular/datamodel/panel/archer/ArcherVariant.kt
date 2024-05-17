package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherVariant(
    val gene: String,
    val hgvsCodingImpact: String,
    val chromosome: String? = null,
    val position: Int? = null,
    val ref: String? = null,
    val alt: String? = null,
    val type: VariantType? = null,
    val codingEffect: CodingEffect? = null,
    val annotation: ArcherVariantAnnotation? = null
) : PanelEvent {

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene $hgvsCodingImpact"
    }
}