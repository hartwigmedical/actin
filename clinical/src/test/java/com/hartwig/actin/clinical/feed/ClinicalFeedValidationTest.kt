package com.hartwig.actin.clinical.feed

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.feed.ClinicalFeedValidation.validate
import com.hartwig.actin.clinical.feed.patient.ImmutablePatientEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import org.junit.Test

class ClinicalFeedValidationTest {
    @Test
    fun standardTestFeedIsValid() {
        validate(TestFeedFactory.createTestClinicalFeed())
    }

    @Test(expected = IllegalStateException::class)
    fun doNotAllowDuplicatePatients() {
        val basisFeed = TestFeedFactory.createTestClinicalFeed()
        val patients: List<PatientEntry> = Lists.newArrayList<PatientEntry>(
            ImmutablePatientEntry.builder().from(
                basisFeed.patientEntries()[0]
            ).build(),
            ImmutablePatientEntry.builder().from(basisFeed.patientEntries()[0]).build()
        )
        validate(ImmutableClinicalFeed.builder().from(basisFeed).patientEntries(patients).build())
    }
}