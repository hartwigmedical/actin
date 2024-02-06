package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.feed.emc.ClinicalFeedValidation.validate
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import org.junit.Test

class EmcClinicalFeedValidationTest {
    @Test
    fun standardTestFeedIsValid() {
        validate(TestFeedFactory.createTestClinicalFeed())
    }

    @Test(expected = IllegalStateException::class)
    fun doNotAllowDuplicatePatients() {
        val basisFeed = TestFeedFactory.createTestClinicalFeed()
        val patients: List<PatientEntry> = listOf(basisFeed.patientEntries[0], basisFeed.patientEntries[0])
        validate(basisFeed.copy(patientEntries = patients))
    }
}