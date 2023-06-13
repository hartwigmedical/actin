package com.hartwig.actin.clinical.feed;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.patient.ImmutablePatientEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;

import org.junit.Test;

public class ClinicalFeedValidationTest {

    @Test
    public void standardTestFeedIsValid() {
        ClinicalFeedValidation.validate(TestFeedFactory.createTestClinicalFeed());
    }

    @Test(expected = IllegalStateException.class)
    public void doNotAllowDuplicatePatients() {
        ClinicalFeed basisFeed = TestFeedFactory.createTestClinicalFeed();

        List<PatientEntry> patients = Lists.newArrayList(ImmutablePatientEntry.builder().from(basisFeed.patientEntries().get(0)).build(),
                ImmutablePatientEntry.builder().from(basisFeed.patientEntries().get(0)).build());

        ClinicalFeedValidation.validate(ImmutableClinicalFeed.builder().from(basisFeed).patientEntries(patients).build());
    }
}