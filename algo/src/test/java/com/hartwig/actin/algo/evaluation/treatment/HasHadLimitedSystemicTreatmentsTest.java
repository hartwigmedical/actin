package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHadLimitedSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSystemicTreatments function = new HasHadLimitedSystemicTreatments(1);

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.PASS, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-systemic
        priorTumorTreatments.add(ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(false).build());
        assertEquals(Evaluation.PASS, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));

        // Add one systemic
        priorTumorTreatments.add(ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true).build());
        assertEquals(Evaluation.PASS, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));

        // Add one more systemic
        priorTumorTreatments.add(ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true).build());
        assertEquals(Evaluation.FAIL, function.evaluate(withPriorTumorTreatments(priorTumorTreatments)));
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