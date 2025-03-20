package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.orange.AminoAcid.forceSingleLetterAminoAcids

object ImpactDisplay {
    fun formatVariantImpact(
        hgvsProteinImpact: String,
        hgvsCodingImpact: String,
        isSplice: Boolean,
        isUpstream: Boolean,
        effects: String
    ): String {
        if (hgvsProteinImpact.isNotEmpty() && hgvsProteinImpact != "p.?") {
            val codingImpact = if (hgvsCodingImpact.isNotEmpty()) " ($hgvsCodingImpact)" else ""
            return forceSingleLetterAminoAcids(hgvsProteinImpact.removePrefix("p.")) + codingImpact
        }

        if (hgvsCodingImpact.isNotEmpty()) {
            return if (isSplice) "$hgvsCodingImpact splice" else hgvsCodingImpact
        }

        return if (isUpstream) "upstream" else effects
    }
}