package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class GenericFusionExtraction(
    val geneStart: String,
    val geneEnd: String,
) : PanelEvent {
    companion object {
        fun parseFusion(text: String): GenericFusionExtraction {
            val parts = text.trim().split("::")
            if (parts.size != 2) {
                throw IllegalArgumentException("Expected two parts in fusion but got ${parts.size} for $text")
            }

            return GenericFusionExtraction(parts[0], parts[1])
        }
    }

    override fun display(): String {
        return "$geneStart-$geneEnd fusion"
    }

    override fun impactsGene(gene: String): Boolean {
        return geneStart == gene || geneEnd == gene
    }
}
