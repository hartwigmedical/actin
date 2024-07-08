package com.hartwig.actin.molecular.paver

data class PaverConfig(
    val ensemblDataDir: String,
    val refGenomeFasta: String,
    val refGenomeVersion: String,  // TODO use constant
    val tempDir: String,
)
