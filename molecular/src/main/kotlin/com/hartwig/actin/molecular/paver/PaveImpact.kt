package com.hartwig.actin.molecular.paver

//##INFO=<ID=IMPACT,Number=10,Type=String,Description="Variant Impact [Gene, Transcript, CanonicalEffect, CanonicalCodingEffect, SpliceRegion, HgvsCodingImpact, HgvsProteinImpact, OtherReportableEffects, WorstCodingEffect, GenesAffected]">

data class PaveImpact(
    val gene: String,
    val transcript: String,
    val canonicalEffect: String,
    val canonicalCodingEffect: String,
    val spliceRegion: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val otherReportableEffects: String,
    val worstCodingEffect: String,
    val genesAffected: String,
)
