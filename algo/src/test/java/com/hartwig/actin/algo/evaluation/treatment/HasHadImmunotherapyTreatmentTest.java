package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadImmunotherapyTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadImmunotherapyTreatment function = new HasHadImmunotherapyTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-immunotherapy
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immuno/radiotherapy
        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.RADIOTHERAPY, TreatmentCategory.IMMUNOTHERAPY)
                .build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}