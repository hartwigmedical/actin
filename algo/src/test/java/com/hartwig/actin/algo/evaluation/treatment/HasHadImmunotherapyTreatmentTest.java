package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadImmunotherapyTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadImmunotherapyTreatment function = new HasHadImmunotherapyTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-immunotherapy
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immuno/radiotherapy
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder()
                .addCategories(TreatmentCategory.RADIOTHERAPY, TreatmentCategory.IMMUNOTHERAPY)
                .build());
        assertEquals(EvaluationResult.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }
}