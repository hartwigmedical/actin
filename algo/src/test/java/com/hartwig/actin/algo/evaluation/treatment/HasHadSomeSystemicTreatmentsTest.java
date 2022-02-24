package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadSomeSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadSomeSystemicTreatments function = new HasHadSomeSystemicTreatments(1);

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-systemic
        priorTumorTreatments.add(TreatmentTestFactory.builder().isSystemic(false).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one systemic
        priorTumorTreatments.add(TreatmentTestFactory.builder().isSystemic(true).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one more systemic
        priorTumorTreatments.add(TreatmentTestFactory.builder().isSystemic(true).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}