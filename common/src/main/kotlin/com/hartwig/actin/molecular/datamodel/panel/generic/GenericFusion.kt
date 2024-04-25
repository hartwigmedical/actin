package com.hartwig.actin.molecular.datamodel.panel.generic

data class GenericFusion(
    val geneStart: String,
    val geneEnd: String,
) {
    companion object {
        fun parseFusion(text: String): GenericFusion {
            val parts = text.trim().split("::")
            if (parts.size != 2) {
                throw IllegalArgumentException("Expected two parts in fusion but got ${parts.size} for $text")
            }

            return GenericFusion(parts[0], parts[1])
        }
    }
}
