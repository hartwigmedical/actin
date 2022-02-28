package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadLimitedTreatmentsWithCategoryOfTypesTest {

    @Test
    public void canEvaluate() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        List<String> types = Lists.newArrayList("anti-BRAF", "anti-KRAS");
        HasHadLimitedTreatmentsWithCategoryOfTypes function = new HasHadLimitedTreatmentsWithCategoryOfTypes(category, types, 1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-EGFR").build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-BRAF").build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with another correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-KRAS").build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}