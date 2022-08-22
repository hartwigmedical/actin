package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadLimitedSpecificTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSpecificTreatments function =
                new HasHadLimitedSpecificTreatments(Sets.newHashSet("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY, 1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment
        treatments.add(TreatmentTestFactory.builder().name("wrong treatment").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").addCategories(TreatmentCategory.CHEMOTHERAPY).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add unclear treatment with correct category.
        treatments.add(TreatmentTestFactory.builder().name("other treatment").addCategories(TreatmentCategory.CHEMOTHERAPY).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 2").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateWithTrials() {
        HasHadLimitedSpecificTreatments function = new HasHadLimitedSpecificTreatments(Sets.newHashSet("right treatment"), null, 1);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();

        // Add correct treatment within trial
        treatments.add(TreatmentTestFactory.builder().name("right treatment").addCategories(TreatmentCategory.TRIAL).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add aonther trial with unclear treatment
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canHandleNoWarnCategory() {
        HasHadLimitedSpecificTreatments function = new HasHadLimitedSpecificTreatments(Sets.newHashSet("treatment"), null, 1);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        treatments.add(TreatmentTestFactory.builder().name("treatment").build());
        treatments.add(TreatmentTestFactory.builder().name("treatment").build());

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}