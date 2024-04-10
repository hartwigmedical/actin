package com.hartwig.actin.clinical.feed.standard

import java.time.LocalDate

const val HASHED_ID_IN_BASE64 = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0="
const val TREATMENT_NAME = "treatmentName"
const val MODIFICATION_NAME = "modificationName"
const val PRIOR_CONDITION_INPUT = "prior condition"
const val TREATMENT_HISTORY_INPUT = "treatment name"

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


    fun createEhrTreatmentHistory() = EhrTreatmentHistory(
        treatmentName = TREATMENT_NAME,
        administeredCycles = 1,
        intendedCycles = 1,
        startDate = LocalDate.of(2024, 2, 23),
        administeredInStudy = false,
        intention = "Palliative",
        stopReason = "TOXICITY",
        endDate = LocalDate.of(2024, 2, 27),
        response = "COMPLETE_RESPONSE",
        modifications = listOf(
            createEhrModification()
        )
    )

    fun createEhrModification() = EhrTreatmentModification(
        name = MODIFICATION_NAME, administeredCycles = 2, date = LocalDate.of(2024, 2, 23)
    )
}