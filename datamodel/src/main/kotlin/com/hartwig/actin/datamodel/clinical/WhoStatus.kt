package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class WhoStatus(
    val date: LocalDate,
    val status: Int
)