package com.hartwig.actin.clinical.feed.standard

import java.time.LocalDate

object EhrTestData {

    fun createEhrPatientRecord() = EhrPatientRecord(
        patientDetails = EhrPatientDetail(
            hashedId = "hashedId",
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
        measurableDisease = false,
        tumorGradeDifferentiation = "tumorGradeDifferentiation",
    )


}