package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentCategoryTest {

    @Test
    public void canEvaluate() {
        HasHadTreatmentCategory any = new HasHadTreatmentCategory(TreatmentCategory.TARGETED_THERAPY, null);
        HasHadTreatmentCategory specific = new HasHadTreatmentCategory(TreatmentCategory.TARGETED_THERAPY, "Anti-EGFR");

        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(EvaluationResult.FAIL, any.evaluate(noPriorTreatmentRecord).result());
        assertEquals(EvaluationResult.FAIL, specific.evaluate(noPriorTreatmentRecord).result());

        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(EvaluationResult.FAIL, any.evaluate(immunoRecord).result());
        assertEquals(EvaluationResult.FAIL, specific.evaluate(immunoRecord).result());

        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(EvaluationResult.PASS, any.evaluate(multiRecord1).result());
        assertEquals(EvaluationResult.FAIL, specific.evaluate(multiRecord1).result());

        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some Anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertEquals(EvaluationResult.PASS, any.evaluate(multiRecord2).result());
        assertEquals(EvaluationResult.PASS, specific.evaluate(multiRecord2).result());
    }
}