package com.hartwig.actin.treatment.ctc.config

data class CTCDatabase(
    val entries: List<CTCDatabaseEntry>,
    val studyMETCsToIgnore: Set<String>,
    val unmappedCohortIds: Set<Int>
)