package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadSomeTreatmentsWithCategoryTest {

    @Test
    public void canEvaluate() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        HasHadSomeTreatmentsWithCategory function = new HasHadSomeTreatmentsWithCategory(category, 2);

        // No treatments yet
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add wrong treatment category
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add correct treatment category
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(category).build());
        assertFalse(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add another correct treatment category
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(category).build());
        assertTrue(function.isPass(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}