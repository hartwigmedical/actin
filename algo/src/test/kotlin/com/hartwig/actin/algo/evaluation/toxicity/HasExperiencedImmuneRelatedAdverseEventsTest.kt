package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasExperiencedImmuneRelatedAdverseEventsTest {

    @Test
    public void canEvaluate() {
        HasExperiencedImmuneRelatedAdverseEvents function = new HasExperiencedImmuneRelatedAdverseEvents();

        // No prior treatments
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withPriorTumorTreatments(treatments)));

        // Treatment with mismatch category
        treatments.add(ToxicityTestFactory.treatment().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withPriorTumorTreatments(treatments)));

        // Treatment with matching category
        treatments.add(ToxicityTestFactory.treatment().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withPriorTumorTreatments(treatments)));
    }
}