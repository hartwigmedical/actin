package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.orange.AminoAcid.forceSingleLetterAminoAcids

object FormatFunctions {
    
    fun formatVariantImpact(
        hgvsProteinImpact: String,
        hgvsCodingImpact: String,
        isSplice: Boolean,
        isUpstream: Boolean,
        effects: String
    ): String {
        if (hgvsProteinImpact.isNotEmpty() && hgvsProteinImpact != "p.?") {
            return forceSingleLetterAminoAcids(hgvsProteinImpact.removePrefix("p."))
        }

        if (hgvsCodingImpact.isNotEmpty()) {
            return if (isSplice) "$hgvsCodingImpact splice" else hgvsCodingImpact
        }

        return if (isUpstream) "upstream" else effects
    }

    fun formatFusionEvent(geneUp: String?, exonUp: Int?, geneDown: String?, exonDown: Int?): String {
        val formatUp = formatGeneAndExon(geneUp, exonUp)
        val formatDown = formatGeneAndExon(geneDown, exonDown)

        if (formatUp == null && formatDown == null) {
            throw IllegalStateException("Fusion cannot be formatted because geneUp and geneDown are null")
        }
        return listOfNotNull(formatUp, formatDown).joinToString("::") + " fusion"
    }

    private fun formatGeneAndExon(gene: String?, exon: Int?): String? {
        return gene?.let { exon?.let { "$gene(exon$exon)" } ?: gene }
    }
}