package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
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
        assertFalse(any.isPass(noPriorTreatmentRecord));
        assertFalse(specific.isPass(noPriorTreatmentRecord));

        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertFalse(any.isPass(immunoRecord));
        assertFalse(specific.isPass(immunoRecord));

        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertTrue(any.isPass(multiRecord1));
        assertFalse(specific.isPass(multiRecord1));

        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertTrue(any.isPass(multiRecord2));
        assertTrue(specific.isPass(multiRecord2));
    }
}