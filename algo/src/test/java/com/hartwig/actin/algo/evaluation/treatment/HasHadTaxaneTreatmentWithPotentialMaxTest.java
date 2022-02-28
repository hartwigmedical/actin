package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadTaxaneTreatmentWithPotentialMaxTest {

    @Test
    public void canEvaluate() {
        HasHadTaxaneTreatmentWithPotentialMax noMax = new HasHadTaxaneTreatmentWithPotentialMax(null);
        HasHadTaxaneTreatmentWithPotentialMax withMax = new HasHadTaxaneTreatmentWithPotentialMax(1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertFalse(noMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
        assertFalse(withMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add a random non-taxane treatment
        treatments.add(TreatmentTestFactory.builder().name("some random treatment").build());
        assertFalse(noMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
        assertFalse(withMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        String firstValidTreatment = HasHadTaxaneTreatmentWithPotentialMax.TAXANE_TREATMENTS.iterator().next();
        // Add a random taxane treatment
        treatments.add(TreatmentTestFactory.builder().name(firstValidTreatment).build());
        assertTrue(noMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
        assertTrue(withMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another random taxane treatment
        treatments.add(TreatmentTestFactory.builder().name(firstValidTreatment).build());
        assertTrue(noMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
        assertFalse(withMax.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}