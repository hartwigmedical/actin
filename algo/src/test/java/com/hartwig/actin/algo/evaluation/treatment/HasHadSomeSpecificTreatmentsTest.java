package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadSomeSpecificTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadSomeSpecificTreatments function =
                new HasHadSomeSpecificTreatments(Sets.newHashSet("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY, 1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment
        treatments.add(TreatmentTestFactory.builder().name("wrong treatment").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add right type
        treatments.add(TreatmentTestFactory.builder().name("right type").addCategories(TreatmentCategory.CHEMOTHERAPY).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Different correct treatment
        List<PriorTumorTreatment> different = Lists.newArrayList(TreatmentTestFactory.builder().name("treatment 2").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(different)));
    }

    @Test
    public void canHandleNoWarnCategory() {
        HasHadSomeSpecificTreatments function = new HasHadSomeSpecificTreatments(Sets.newHashSet("treatment"), null, 2);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        treatments.add(TreatmentTestFactory.builder().name("treatment").build());
        treatments.add(TreatmentTestFactory.builder().name("treatment").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}