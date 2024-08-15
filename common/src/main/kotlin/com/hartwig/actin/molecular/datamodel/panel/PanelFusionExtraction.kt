package com.hartwig.actin.molecular.datamodel.panel

data class PanelFusionExtraction(
    val geneUp: String?,
    val geneDown: String?,
) : PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return geneUp == gene || geneDown == gene
    }

    override fun display(): String {
        return when {
            geneUp != null && geneDown == null -> "$geneUp fusion"
            geneUp == null && geneDown != null -> "$geneDown fusion"
            geneUp != null && geneDown != null -> "$geneUp-$geneDown fusion"
            else -> throw IllegalStateException("Both genes in fusion are null")
        }
    }

    companion object {
        fun parseFusion(text: String): PanelFusionExtraction {
            val parts = text.trim().split("::")

            return when (parts.size) {
                1 -> PanelFusionExtraction(parts[0], null)
                2 -> {
                    val geneUp = parts[0].ifEmpty { null }
                    val geneDown = parts[1].ifEmpty { null }
                    PanelFusionExtraction(geneUp, geneDown)
                }

                else -> throw IllegalArgumentException("Unable to parse fusion: $text")
            }
        }
    }
}