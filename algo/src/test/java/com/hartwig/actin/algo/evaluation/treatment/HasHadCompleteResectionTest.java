package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadCompleteResectionTest {

    @Test
    public void canEvaluate() {
        HasHadCompleteResection function = new HasHadCompleteResection();

        // FAIL without any prior tumor treatments.
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(Lists.newArrayList())));

        // PASS on a complete resection
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .name(HasHadCompleteResection.COMPLETE_RESECTION)
                        .build())));

        // UNDETERMINED when the resection is not fully specified.
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .name("some form of " + HasHadCompleteResection.RESECTION_KEYWORD)
                        .build())));

        // UNDETERMINED in case of unspecified surgery
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.SURGERY)
                        .build())));
    }
}