package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentWithCategoryButNotOfTypesTest {

    @Test
    public void canEvaluate() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        List<String> ignoreTypes = Lists.newArrayList("type1", "type2");
        HasHadTreatmentWithCategoryButNotOfTypes function = new HasHadTreatmentWithCategoryButNotOfTypes(category, ignoreTypes);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category but with ignore type 1
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType(ignoreTypes.get(0)).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category but with ignore type 2
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType(ignoreTypes.get(1)).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category and correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("pass me").build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}