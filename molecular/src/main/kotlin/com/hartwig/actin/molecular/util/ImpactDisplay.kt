package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.orange.interpretation.AminoAcid.forceSingleLetterAminoAcids

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