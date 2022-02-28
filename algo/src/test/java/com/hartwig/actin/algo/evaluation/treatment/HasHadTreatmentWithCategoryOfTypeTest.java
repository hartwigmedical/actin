package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentWithCategoryOfTypeTest {

    @Test
    public void canEvaluate() {
        HasHadTreatmentWithCategoryOfType specific = new HasHadTreatmentWithCategoryOfType(TreatmentCategory.TARGETED_THERAPY, "Anti-EGFR");

        // No treatments yet
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertFalse(specific.isPass(noPriorTreatmentRecord));

        // Add one wrong category
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertFalse(specific.isPass(immunoRecord));

        // Add one correct category but wrong type
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertFalse(specific.isPass(multiRecord1));

        // Add one correct category with matching type
        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments);
        assertTrue(specific.isPass(multiRecord2));
    }
}