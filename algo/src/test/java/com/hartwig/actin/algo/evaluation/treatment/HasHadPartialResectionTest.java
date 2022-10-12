package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadPartialResectionTest {

    @Test
    public void canEvaluate() {
        HasHadPartialResection function = new HasHadPartialResection();

        // FAIL without any prior tumor treatments.
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(Lists.newArrayList())));

        // PASS on a partial resection
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .name(HasHadPartialResection.PARTIAL_RESECTION)
                        .build())));

        // UNDETERMINED when the resection is not fully specified.
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .name("some form of " + HasHadPartialResection.RESECTION_KEYWORD)
                        .build())));

        // UNDETERMINED in case of unspecified surgery
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.SURGERY)
                        .build())));
    }
}