package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.junit.Test;

public class LabInterpretationFactoryTest {

    @Test
    public void canGenerateLabInterpretation() {
        assertNotNull(LabInterpretationFactory.fromLabValues(Lists.newArrayList()));

        assertNotNull(LabInterpretationFactory.fromLabValues(TestClinicalDataFactory.createProperTestClinicalRecord().labValues()));
    }
}