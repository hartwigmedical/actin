package com.hartwig.actin.datamodel.molecular.orange.driver

data class ExtendedFusionDetails(
    val geneTranscriptStart: String,
    val geneTranscriptEnd: String,
    val fusedExonUp: Int,
    val fusedExonDown: Int
)