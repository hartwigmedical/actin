package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadTaxaneTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadTaxaneTreatment function = new HasHadTaxaneTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add a random non-taxane treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("some random treatment").build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add a random taxane treatment
        String firstValidTreatment = HasHadTaxaneTreatment.TAXANE_TREATMENTS.iterator().next();
        priorTumorTreatments.add(TreatmentTestFactory.builder().name(firstValidTreatment).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}