package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.ClinicalFeedValidation.validate
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import org.junit.Test

class ClinicalFeedValidationTest {
    @Test
    fun standardTestFeedIsValid() {
        validate(TestFeedFactory.createTestClinicalFeed(), TestAtcFactory.createProperAtcModel())
    }

    @Test(expected = IllegalStateException::class)
    fun doNotAllowDuplicatePatients() {
        val basisFeed = TestFeedFactory.createTestClinicalFeed()
        val patients: List<PatientEntry> = listOf(basisFeed.patientEntries[0], basisFeed.patientEntries[0])
        validate(basisFeed.copy(patientEntries = patients), TestAtcFactory.createProperAtcModel())
    }
}