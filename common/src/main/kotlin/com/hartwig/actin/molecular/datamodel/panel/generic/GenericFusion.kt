package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class GenericFusion(
    val geneStart: String,
    val geneEnd: String,
) : PanelEvent {
    companion object {
        fun parseFusion(text: String): GenericFusion {
            val parts = text.trim().split("::")
            if (parts.size != 2) {
                throw IllegalArgumentException("Expected two parts in fusion but got ${parts.size} for $text")
            }

            return GenericFusion(parts[0], parts[1])
        }
    }

    override fun display(): String {
        return "$geneStart::$geneEnd"
    }

    override fun impactsGene(gene: String): Boolean {
        return geneStart == gene || geneEnd == gene
    }
}
