package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Surgery(
    val name: String? = null,
    val endDate: LocalDate,
    val status: SurgeryStatus
)