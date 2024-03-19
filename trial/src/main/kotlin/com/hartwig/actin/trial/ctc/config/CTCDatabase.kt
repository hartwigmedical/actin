package com.hartwig.actin.trial.ctc.config

data class CTCDatabase(
    val entries: List<CTCDatabaseEntry>,
    val studyMETCsToIgnore: Set<String>,
    val unmappedCohortIds: Set<Int>,
    val mecStudiesNotInCTC: Set<String>
)