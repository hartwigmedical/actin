package com.hartwig.actin.clinical.curation

data class CurationWarning(val message: String)

data class CurationResult(val warnings: List<CurationWarning>)