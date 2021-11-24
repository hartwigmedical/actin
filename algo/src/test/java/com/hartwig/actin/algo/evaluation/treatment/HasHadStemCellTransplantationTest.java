package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHadStemCellTransplantationTest {

    @Test
    public void canEvaluate() {
        HasHadStemCellTransplantation function = new HasHadStemCellTransplantation();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));

        ImmutablePriorTumorTreatment.Builder builder = ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true);

        // Add one treatment without categories
        priorTumorTreatments.add(builder.build());
        assertEquals(Evaluation.FAIL, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immunotherapy + radiotherapy combination
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.IMMUNOTHERAPY, TreatmentCategory.RADIOTHERAPY))
                .build());
        assertEquals(Evaluation.FAIL, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));

        // Add one stem cell transplantation
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.STEM_CELL_TRANSPLANTATION)).build());
        assertEquals(Evaluation.PASS, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));
    }

    @NotNull
    private static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(priorTumorTreatments)
                        .build())
                .build();
    }

}