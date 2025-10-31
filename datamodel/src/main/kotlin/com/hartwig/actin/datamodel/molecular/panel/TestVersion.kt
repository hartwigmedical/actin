package com.hartwig.actin.datamodel.molecular.panel

import java.time.LocalDate

data class TestVersion(
    val versionDate: LocalDate? = null,
    val testDateIsBeforeOldestTestVersion: Boolean = false
)