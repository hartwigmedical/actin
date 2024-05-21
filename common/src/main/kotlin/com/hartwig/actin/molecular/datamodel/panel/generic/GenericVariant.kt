package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class GenericVariant(
    val gene: String,
    val hgvsCodingImpact: String
) : PanelEvent {
    companion object {
        fun parseVariant(priorMolecularTest: PriorMolecularTest): GenericVariant {
            return if (priorMolecularTest.item != null && priorMolecularTest.measure != null) {
                GenericVariant(gene = priorMolecularTest.item, hgvsCodingImpact = priorMolecularTest.measure)
            } else {
                throw IllegalArgumentException("Expected item and measure for variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}")
            }
        }
    }

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene $hgvsCodingImpact"
    }
}