package com.hartwig.actin.clinical.feed.standard

import com.hartwig.feed.datamodel.DatedEntry
import com.hartwig.feed.datamodel.FeedPatientDetail
import com.hartwig.feed.datamodel.FeedPatientRecord
import com.hartwig.feed.datamodel.FeedSurgery
import com.hartwig.feed.datamodel.FeedTumorDetail
import java.time.LocalDate

const val HASHED_ID_IN_BASE64 = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0="
const val TREATMENT_NAME = "treatmentName"
const val SURGERY_NAME = "surgeryName"
const val OTHER_CONDITION_INPUT = "prior condition"
const val TREATMENT_HISTORY_INPUT = "treatment name"
private val DATE_Y2023_M02_D23 = LocalDate.of(2024, 2, 23)

object FeedTestData {

    val FEED_PATIENT_RECORD = FeedPatientRecord(
        patientDetails = FeedPatientDetail(
            patientId = HASHED_ID_IN_BASE64,
            birthYear = 2024,
            gender = "FEMALE",
            registrationDate = DATE_Y2023_M02_D23,
            hartwigMolecularDataExpected = false
        ),
        tumorDetails = createFeedTumorDetail()
    )

    val FEED_TREATMENT_HISTORY = DatedEntry(
        name = TREATMENT_NAME,
        startDate = DATE_Y2023_M02_D23,
        endDate = LocalDate.of(2024, 2, 27),
    )

    fun createFeedSurgery(surgeryName: String? = SURGERY_NAME) = FeedSurgery(
        name = surgeryName,
        endDate = LocalDate.of(2024, 2, 23),
        status = "FINISHED"
    )

    private fun createFeedTumorDetail() = FeedTumorDetail(
        diagnosisDate = DATE_Y2023_M02_D23,
        tumorLocation = "tumorLocation",
        tumorType = "tumorType",
        lesions = emptyList(),
        hasBrainLesions = false,
        hasActiveBrainLesions = false,
        hasBoneLesions = false,
        hasLiverLesions = false,
        measurableDiseaseDate = DATE_Y2023_M02_D23,
        measurableDisease = true,
        tumorGradeDifferentiation = "tumorGradeDifferentiation",
        stage = "IV",
        tumorStageDate = LocalDate.of(2024, 2, 29)
    )
}