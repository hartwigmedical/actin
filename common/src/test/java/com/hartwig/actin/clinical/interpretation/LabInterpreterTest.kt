package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.junit.Test;

public class LabInterpreterTest {

    @Test
    public void canGenerateLabInterpretation() {
        assertNotNull(LabInterpreter.interpret(Lists.newArrayList()));

        assertNotNull(LabInterpreter.interpret(TestClinicalFactory.createProperTestClinicalRecord().labValues()));
    }

    @Test
    public void canMapValues() {
        LabMeasurement firstKey = LabInterpreter.MAPPINGS.keySet().iterator().next();
        LabMeasurement firstValue = LabInterpreter.MAPPINGS.get(firstKey);

        List<LabValue> values = Lists.newArrayList();
        values.add(LabInterpretationTestFactory.builder().code(firstKey.code()).unit(firstKey.defaultUnit()).build());
        values.add(LabInterpretationTestFactory.builder().code(firstValue.code()).unit(firstValue.defaultUnit()).build());

        LabInterpretation interpretation = LabInterpreter.interpret(values);

        assertEquals(1, interpretation.allValues(firstKey).size());
        assertEquals(2, interpretation.allValues(firstValue).size());
        for (LabValue labValue : interpretation.allValues(firstValue)) {
            assertEquals(firstValue.code(), labValue.code());
            assertEquals(firstValue.defaultUnit(), labValue.unit());
        }
    }
}