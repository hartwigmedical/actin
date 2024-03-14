package com.hartwig.actin.clinical.feed.standard

import java.time.LocalDate

const val HASHED_ID_IN_BASE64 = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0="

object EhrTestData {

    fun createEhrPatientRecord() = EhrPatientRecord(
        patientDetails = EhrPatientDetail(
            hashedId = HASHED_ID_IN_BASE64,
            birthYear = 2024,
            gender = "FEMALE",
            registrationDate = LocalDate.of(2024, 2, 23)
        ),
        tumorDetails = createEhrTumorDetail()
    )

    private fun createEhrTumorDetail() = EhrTumorDetail(
        diagnosisDate = LocalDate.of(2024, 2, 23),
        tumorLocation = "tumorLocation",
        tumorType = "tumorType",
        lesions = emptyList(),
        measurableDiseaseDate = LocalDate.of(2024, 2, 23),
        measurableDisease = true,
        tumorGradeDifferentiation = "tumorGradeDifferentiation",
        tumorStage = "IV",
        tumorStageDate = LocalDate.of(2024, 2, 29)
    )


}