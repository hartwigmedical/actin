package com.hartwig.actin.molecular.paver

import com.hartwig.actin.molecular.datamodel.CodingEffect

data class PaveImpact(
    val gene: String,
    val transcript: String,
    val canonicalEffect: String,
    val canonicalCodingEffect: CodingEffect,
    val spliceRegion: Boolean,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val worstCodingEffect: CodingEffect,
    val genesAffected: Int,
)
