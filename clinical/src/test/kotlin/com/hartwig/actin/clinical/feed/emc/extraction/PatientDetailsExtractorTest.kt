package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PatientDetailsExtractorTest {

    private val extractor = PatientDetailsExtractor()
    private val feedRecord = FEED_PATIENT_RECORD

    @Test
    fun `Should extract patient details`() {
        val (extracted, evaluation) = extractor.extract(feedRecord)
        assertThat(extracted).isEqualTo(
            PatientDetails(
                gender = Gender.FEMALE,
                birthYear = 2024,
                registrationDate = LocalDate.of(2024, 2, 23),
                questionnaireDate = LocalDate.of(2024, 2, 23),
                hasHartwigSequencing = true
            )
        )
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract patient details with required data only`() {
        val minPatientDetails = FEED_PATIENT_RECORD.patientDetails.copy(
            hartwigMolecularDataExpected = null,
            questionnaireDate = null
        )
        val (extracted, evaluation) = extractor.extract(feedRecord.copy(patientDetails = minPatientDetails))
        assertThat(extracted).isEqualTo(
            PatientDetails(
                gender = Gender.FEMALE,
                birthYear = 2024,
                registrationDate = LocalDate.of(2024, 2, 23),
                questionnaireDate = null,
                hasHartwigSequencing = true
            )
        )
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception with invalid gender`() {
        extractor.extract(feedRecord.copy(patientDetails = feedRecord.patientDetails.copy(gender = "invalid")))
    }


}