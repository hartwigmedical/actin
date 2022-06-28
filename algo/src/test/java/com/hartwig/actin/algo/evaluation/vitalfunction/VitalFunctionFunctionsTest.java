package com.hartwig.actin.algo.evaluation.vitalfunction;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.junit.Test;

public class VitalFunctionFunctionsTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canSelectMedianFunction() {
        List<VitalFunction> vitalFunctions = Lists.newArrayList();

        ImmutableVitalFunction.Builder builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE);
        vitalFunctions.add(builder.value(1D).build());
        assertEquals(1D, VitalFunctionFunctions.selectMedianFunction(vitalFunctions).value(), EPSILON);

        vitalFunctions.add(builder.value(2D).build());
        assertEquals(1D, VitalFunctionFunctions.selectMedianFunction(vitalFunctions).value(), EPSILON);

        vitalFunctions.add(builder.value(3D).build());
        assertEquals(2D, VitalFunctionFunctions.selectMedianFunction(vitalFunctions).value(), EPSILON);
    }

    @Test
    public void canDetermineMedianValue() {
        List<VitalFunction> vitalFunctions = Lists.newArrayList();

        ImmutableVitalFunction.Builder builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE);
        vitalFunctions.add(builder.value(1D).build());
        assertEquals(1D, VitalFunctionFunctions.determineMedianValue(vitalFunctions), EPSILON);

        vitalFunctions.add(builder.value(2D).build());
        assertEquals(1.5, VitalFunctionFunctions.determineMedianValue(vitalFunctions), EPSILON);

        vitalFunctions.add(builder.value(3D).build());
        assertEquals(2D, VitalFunctionFunctions.determineMedianValue(vitalFunctions), EPSILON);
    }
}