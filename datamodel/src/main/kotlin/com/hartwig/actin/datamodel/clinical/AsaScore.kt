package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class AsaScore(
    val date: LocalDate,
    val score: Int
)