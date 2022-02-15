package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadLimitedAntiPDL1OrPD1ImmunotherapiesTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedAntiPDL1OrPD1Immunotherapies function = new HasHadLimitedAntiPDL1OrPD1Immunotherapies(1);

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(EvaluationResult.PASS,
                function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add one immunotherapy with null type
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add one immunotherapy with another type
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder()
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .immunoType("Anti-CTLA-4")
                .build());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add one immunotherapy with PD1 type
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder()
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .immunoType(HasHadLimitedAntiPDL1OrPD1Immunotherapies.PD1_TYPE)
                .build());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add one immunotherapy with PDL1 type
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder()
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .immunoType(HasHadLimitedAntiPDL1OrPD1Immunotherapies.PDL1_TYPE)
                .build());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)).result());
    }
}