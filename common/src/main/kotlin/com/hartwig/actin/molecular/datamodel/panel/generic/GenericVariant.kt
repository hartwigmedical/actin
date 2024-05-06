package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect

data class GenericVariant(
    val gene: String,
    val hgvsCodingImpact: String? = null,
    val affectedExon: Int? = null,
    val effects: Set<VariantEffect> = emptySet()
) : PanelEvent {
    companion object {
        fun parseVariant(priorMolecularTest: PriorMolecularTest): GenericVariant {
            return if (priorMolecularTest.item != null && priorMolecularTest.measure != null) {
                val exonMatch = Regex("ex(\\d+) del").find(priorMolecularTest.measure)
                if (exonMatch != null) {
                    val exon = exonMatch.groupValues[1].toInt()
                    GenericVariant(gene = priorMolecularTest.item, affectedExon = exon, effects = setOf(VariantEffect.INFRAME_DELETION))
                } else {
                    GenericVariant(gene = priorMolecularTest.item, hgvsCodingImpact = priorMolecularTest.measure)
                }
            } else {
                throw IllegalArgumentException("Expected gene and variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}")
            }
        }
    }

    override fun event(): String {
        return "$gene $hgvsCodingImpact"
    }
}