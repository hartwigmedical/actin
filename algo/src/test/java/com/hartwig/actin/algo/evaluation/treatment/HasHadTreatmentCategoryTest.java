package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentCategoryTest {

    @Test
    public void canEvaluate() {
        HasHadTreatmentCategory any = new HasHadTreatmentCategory(TreatmentCategory.TARGETED_THERAPY, null);
        HasHadTreatmentCategory specific = new HasHadTreatmentCategory(TreatmentCategory.TARGETED_THERAPY, "Anti-EGFR");

        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(Evaluation.FAIL, any.evaluate(noPriorTreatmentRecord));
        assertEquals(Evaluation.FAIL, specific.evaluate(noPriorTreatmentRecord));

        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(Evaluation.FAIL, any.evaluate(immunoRecord));
        assertEquals(Evaluation.FAIL, specific.evaluate(immunoRecord));

        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(Evaluation.PASS, any.evaluate(multiRecord1));
        assertEquals(Evaluation.FAIL, specific.evaluate(multiRecord1));

        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some Anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(Evaluation.PASS, any.evaluate(multiRecord2));
        assertEquals(Evaluation.PASS, specific.evaluate(multiRecord2));
    }
}