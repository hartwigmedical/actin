package com.hartwig.actin.molecular.paver

data class PaveImpact(
    val gene: String,
    val canonicalTranscript: String,
    val canonicalEffects: List<PaveVariantEffect>,
    val canonicalCodingEffect: PaveCodingEffect,
    val spliceRegion: Boolean,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val otherReportableEffects: String?,
    val worstCodingEffect: PaveCodingEffect,
    val genesAffected: Int,
)
