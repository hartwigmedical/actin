package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadLimitedSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSystemicTreatments function = new HasHadLimitedSystemicTreatments(1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one non-systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(true).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one more systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(true).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}