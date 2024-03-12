package com.hartwig.actin.clinical.feed.standard

import java.time.LocalDate

const val HASHED_ID_IN_HEX = "f44f6e61b16fa450e32550acf578c3185d4b98ff0fa3a65bf34a589e806b5a0d"
const val HASHED_ID_IN_BASE64 = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0="

object EhrTestData {

    fun createEhrPatientRecord() = EhrPatientRecord(
        patientDetails = EhrPatientDetail(
            hashedId = HASHED_ID_IN_HEX,
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