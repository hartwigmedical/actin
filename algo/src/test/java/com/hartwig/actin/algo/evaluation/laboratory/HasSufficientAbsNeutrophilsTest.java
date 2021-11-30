package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientAbsNeutrophilsTest {

    @Test
    public void canEvaluate() {
        HasSufficientAbsNeutrophils function = new HasSufficientAbsNeutrophils(1.5);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder neutrophils1 = LaboratoryTestUtil.builder().code(LabMeasurement.NEUTROPHILS_ABS.code());
        ImmutableLabValue.Builder neutrophils2 = LaboratoryTestUtil.builder().code(LabMeasurement.NEUTROPHILS_ABS_EDA.code());

        assertEquals(Evaluation.PASS,
                function.evaluate(LaboratoryTestUtil.withLabValues(Lists.newArrayList(neutrophils1.value(1.2).build(),
                        neutrophils2.value(4D).build()))));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(neutrophils1.value(1.2).build())));
    }

    @Test
    public void canPickBest() {
        ImmutableLabValue.Builder neutrophils = LaboratoryTestUtil.builder().code(LabMeasurement.NEUTROPHILS_ABS.code());

        LabValue worst = neutrophils.value(1D).build();
        LabValue best = neutrophils.value(2D).build();

        assertNull(HasSufficientAbsNeutrophils.pickBest(null, null));
        assertEquals(worst, HasSufficientAbsNeutrophils.pickBest(worst, null));
        assertEquals(best, HasSufficientAbsNeutrophils.pickBest(null, best));
        assertEquals(best, HasSufficientAbsNeutrophils.pickBest(worst, best));
        assertEquals(best, HasSufficientAbsNeutrophils.pickBest(best, worst));
    }
}