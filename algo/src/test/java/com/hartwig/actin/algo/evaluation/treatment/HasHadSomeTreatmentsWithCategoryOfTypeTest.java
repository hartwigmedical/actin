package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadSomeTreatmentsWithCategoryOfTypeTest {

    @Test
    public void canEvaluate() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        HasHadSomeTreatmentsWithCategoryOfType function = new HasHadSomeTreatmentsWithCategoryOfType(category, "Anti-EGFR", 2);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some other type").build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment category with right type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("Some anti-EGFR treatment").build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment category with right type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("Another anti-EGFR").build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}