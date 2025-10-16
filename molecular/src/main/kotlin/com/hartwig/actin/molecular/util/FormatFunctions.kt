package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.orange.AminoAcid.forceSingleLetterAminoAcids

object FormatFunctions {

    fun formatVariantImpact(
        hgvsProteinImpact: String?,
        hgvsCodingImpact: String?,
        isSplice: Boolean,
        isUpstream: Boolean,
        effects: String
    ): String {
        return when {
            !hgvsProteinImpact.isNullOrEmpty() && hgvsProteinImpact != "p.?" -> {
                forceSingleLetterAminoAcids(hgvsProteinImpact.removePrefix("p."))
            }

            !hgvsCodingImpact.isNullOrEmpty() -> if (isSplice) "$hgvsCodingImpact splice" else hgvsCodingImpact

            hgvsProteinImpact == "" && hgvsCodingImpact == "" -> if (isUpstream) "upstream" else effects

            else -> throw IllegalStateException("Variant impact cannot be formatted - protein and coding impact illegal state")
        }
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