package com.hartwig.actin.molecular.datamodel.orange.driver

data class ExtendedFusionDetails(
    val geneTranscriptStart: String,
    val geneTranscriptEnd: String,
    val fusedExonUp: Int,
    val fusedExonDown: Int
)