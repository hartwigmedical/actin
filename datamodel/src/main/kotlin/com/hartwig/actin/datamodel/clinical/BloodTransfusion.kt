package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class BloodTransfusion(
    val date: LocalDate,
    val product: String
)
