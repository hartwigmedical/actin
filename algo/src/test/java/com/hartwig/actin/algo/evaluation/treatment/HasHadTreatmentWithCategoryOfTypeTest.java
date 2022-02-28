package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        HasHadTreatmentWithCategoryOfType function = new HasHadTreatmentWithCategoryOfType(TreatmentCategory.TARGETED_THERAPY, "Anti-EGFR");

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertFalse(function.isPass(noPriorTreatmentRecord));

        // Add one wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertFalse(function.isPass(immunoRecord));

        // Add one correct category but wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertFalse(function.isPass(multiRecord1));

        // Add one correct category with matching type
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertTrue(function.isPass(multiRecord2));
    }
}