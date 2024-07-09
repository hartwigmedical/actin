package com.hartwig.actin.molecular.paver

import com.hartwig.actin.molecular.datamodel.VariantEffect

//##INFO=<ID=PAVE_TI,Number=.,Type=String,Description="Transcript impact [Gene|GeneName|Transcript|Effects|SpliceRegion|HGVS.c|HGVS.p]">

data class PaveTranscriptImpact(
    val gene: String,
    val geneName: String,
    val transcript: String,
    val effects: List<VariantEffect>,
    val spliceRegion: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
)