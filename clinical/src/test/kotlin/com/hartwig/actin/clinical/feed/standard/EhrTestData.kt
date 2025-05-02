package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientDetail
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.provided.ProvidedSurgery
import com.hartwig.actin.datamodel.clinical.provided.ProvidedTreatmentHistory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedTreatmentModification
import com.hartwig.actin.datamodel.clinical.provided.ProvidedTumorDetail
import java.time.LocalDate

const val HASHED_ID_IN_BASE64 = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0="
const val TREATMENT_NAME = "treatmentName"
const val SURGERY_NAME = "surgeryName"
const val MODIFICATION_NAME = "modificationName"
const val OTHER_CONDITION_INPUT = "prior condition"
const val TREATMENT_HISTORY_INPUT = "treatment name"
private val DATE_Y2023_M02_D23 = LocalDate.of(2024, 2, 23)

object EhrTestData {

    fun createEhrPatientRecord() = ProvidedPatientRecord(
        patientDetails = ProvidedPatientDetail(
            hashedId = HASHED_ID_IN_BASE64,
            birthYear = 2024,
            gender = "FEMALE",
            registrationDate = DATE_Y2023_M02_D23,
            hartwigMolecularDataExpected = false
        ),
        tumorDetails = createEhrTumorDetail()
    )

    private fun createEhrTumorDetail() = ProvidedTumorDetail(
        diagnosisDate = DATE_Y2023_M02_D23,
        tumorLocation = "tumorLocation",
        tumorType = "tumorType",
        lesions = emptyList(),
        measurableDiseaseDate = DATE_Y2023_M02_D23,
        measurableDisease = true,
        tumorGradeDifferentiation = "tumorGradeDifferentiation",
        tumorStage = "IV",
        tumorStageDate = LocalDate.of(2024, 2, 29)
    )

    fun createEhrTreatmentHistory() = ProvidedTreatmentHistory(
        treatmentName = TREATMENT_NAME,
        administeredCycles = 1,
        intendedCycles = 1,
        startDate = DATE_Y2023_M02_D23,
        administeredInStudy = false,
        intention = "Palliative",
        stopReason = "TOXICITY",
        endDate = LocalDate.of(2024, 2, 27),
        response = "COMPLETE_RESPONSE",
        modifications = listOf(
            createEhrModification()
        )
    )

    fun createEhrSurgery(surgeryName: String? = SURGERY_NAME) = ProvidedSurgery(
        surgeryName = surgeryName,
        endDate = LocalDate.of(2024, 2, 23),
        status = "FINISHED"
    )

    fun createEhrModification() = ProvidedTreatmentModification(
        name = MODIFICATION_NAME,
        administeredCycles = 2,
        date = DATE_Y2023_M02_D23
    )
}