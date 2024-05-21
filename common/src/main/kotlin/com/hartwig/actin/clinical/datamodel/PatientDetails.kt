package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class PatientDetails(
    val gender: Gender,
    val birthYear: Int,
    val registrationDate: LocalDate,
    val questionnaireDate: LocalDate? = null
)
