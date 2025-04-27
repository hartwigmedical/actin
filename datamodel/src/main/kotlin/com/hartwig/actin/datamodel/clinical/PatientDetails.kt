package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class PatientDetails(
    val gender: Gender,
    val birthYear: Int,
    val registrationDate: LocalDate,
    val questionnaireDate: LocalDate? = null,
    val hasHartwigSequencing: Boolean,
    val hospitalPatientId: String? = null
)
