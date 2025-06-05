package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Surgery(
    val name: String?,
    val endDate: LocalDate?,
    val status: SurgeryStatus
)