package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.junit.Test;

public class LabInterpreterTest {

    @Test
    public void canGenerateLabInterpretation() {
        assertNotNull(LabInterpreter.interpret(Lists.newArrayList()));

        assertNotNull(LabInterpreter.interpret(TestClinicalDataFactory.createProperTestClinicalRecord().labValues()));
    }
}