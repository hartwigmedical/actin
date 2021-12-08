package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadStemCellTransplantationTest {

    @Test
    public void canEvaluate() {
        HasHadStemCellTransplantation function = new HasHadStemCellTransplantation();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one treatment without categories
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immunotherapy + radiotherapy combination
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder()
                .addCategories(TreatmentCategory.IMMUNOTHERAPY, TreatmentCategory.RADIOTHERAPY)
                .build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one stem cell transplantation
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().addCategories(TreatmentCategory.STEM_CELL_TRANSPLANTATION).build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }
}