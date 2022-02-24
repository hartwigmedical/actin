package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadSpecificTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadSpecificTreatment function = new HasHadSpecificTreatment("treatment 1");

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add wrong treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("treatment 2").build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add correct treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("treatment 1").build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}