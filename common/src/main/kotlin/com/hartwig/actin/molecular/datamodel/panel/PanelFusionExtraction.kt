package com.hartwig.actin.molecular.datamodel.panel

data class PanelFusionExtraction(
    val geneUp: String?,
    val geneDown: String?,
) : PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return geneUp == gene || geneDown == gene
    }

    override fun display(): String {
        return "${geneUp ?: ""}::${geneDown ?: ""}"
    }

    companion object {
        fun parseFusion(text: String): PanelFusionExtraction {
            // TODO change this to handle single genes
            val parts = text.trim().split("::")
            if (parts.size != 2) {
                throw IllegalArgumentException("Expected two parts in fusion but got ${parts.size} for $text")
            }

            return PanelFusionExtraction(parts[0], parts[1])
        }
    }
}