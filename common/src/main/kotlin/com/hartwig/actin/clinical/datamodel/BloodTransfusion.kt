package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class BloodTransfusion(
    val date: LocalDate,
    val product: String
)
