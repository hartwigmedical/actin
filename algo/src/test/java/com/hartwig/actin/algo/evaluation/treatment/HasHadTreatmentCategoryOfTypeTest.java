package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentCategoryOfTypeTest {

    @Test
    public void canEvaluate() {
        HasHadTreatmentCategoryOfType any = new HasHadTreatmentCategoryOfType(TreatmentCategory.TARGETED_THERAPY, null);
        HasHadTreatmentCategoryOfType specific = new HasHadTreatmentCategoryOfType(TreatmentCategory.TARGETED_THERAPY, "Anti-EGFR");

        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEvaluation(EvaluationResult.FAIL, any.evaluate(noPriorTreatmentRecord));
        assertEvaluation(EvaluationResult.FAIL, specific.evaluate(noPriorTreatmentRecord));

        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEvaluation(EvaluationResult.FAIL, any.evaluate(immunoRecord));
        assertEvaluation(EvaluationResult.FAIL, specific.evaluate(immunoRecord));

        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEvaluation(EvaluationResult.PASS, any.evaluate(multiRecord1));
        assertEvaluation(EvaluationResult.FAIL, specific.evaluate(multiRecord1));

        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEvaluation(EvaluationResult.PASS, any.evaluate(multiRecord2));
        assertEvaluation(EvaluationResult.PASS, specific.evaluate(multiRecord2));
    }
}