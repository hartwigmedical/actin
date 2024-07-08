package com.hartwig.actin.molecular.paver

//##INFO=<ID=PAVE_TI,Number=.,Type=String,Description="Transcript impact [Gene|GeneName|Transcript|Effects|SpliceRegion|HGVS.c|HGVS.p]">

class PaveTranscriptImpact(
    val gene: String,
    val geneName: String,
    val transcript: String,
    val effects: String,
    val spliceRegion: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
)