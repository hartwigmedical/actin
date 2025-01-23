package com.hartwig.actin.datamodel.molecular.driver

data class ExtendedFusionDetails(
    val geneTranscriptStart: String,
    val geneTranscriptEnd: String,
    val fusedExonUp: Int,
    val fusedExonDown: Int
)