package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import java.time.LocalDate

data class Surgery(
    val name: String?,
    val endDate: LocalDate?,
    val status: SurgeryStatus,
    val treatmentType: OtherTreatmentType,
)