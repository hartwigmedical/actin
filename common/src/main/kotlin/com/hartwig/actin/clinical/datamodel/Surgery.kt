package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class Surgery(
    val endDate: LocalDate,
    val status: SurgeryStatus
)