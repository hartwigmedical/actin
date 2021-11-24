package com.hartwig.actin.treatment.util;

import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.junit.Test;

public class EligibilityFunctionDisplayTest {

    @Test
    public void canFormatAllTestFunctions() {
        Trial trial = TestTreatmentFactory.createProperTestTrial();

        for (Eligibility eligibility : trial.generalEligibility()) {
            assertNotNull(EligibilityFunctionDisplay.format(eligibility.function()));
        }
    }
}