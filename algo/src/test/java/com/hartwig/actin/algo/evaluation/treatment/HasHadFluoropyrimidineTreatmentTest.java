package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadFluoropyrimidineTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadFluoropyrimidineTreatment function = new HasHadFluoropyrimidineTreatment();

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add a random non-fluoropyrimidine treatment
        treatments.add(TreatmentTestFactory.builder().name("some random treatment").build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add a random fluoropyrimidine treatment
        String firstValidTreatment = HasHadFluoropyrimidineTreatment.FLUOROPYRIMIDINE_TREATMENTS.iterator().next();
        treatments.add(TreatmentTestFactory.builder().name(firstValidTreatment).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}