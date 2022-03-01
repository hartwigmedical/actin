package com.hartwig.actin.algo.evaluation.surgery;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHadRecentSurgeryTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2020, 2, 20);

    @Test
    public void canEvaluate() {
        HasHadRecentSurgery function = new HasHadRecentSurgery(REFERENCE_DATE.minusWeeks(4));

        List<Surgery> surgeries = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgeries(surgeries)));

        surgeries.add(ImmutableSurgery.builder().endDate(REFERENCE_DATE.minusWeeks(8)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgeries(surgeries)));

        surgeries.add(ImmutableSurgery.builder().endDate(REFERENCE_DATE.minusWeeks(2)).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withSurgeries(surgeries)));
    }

    @NotNull
    private static PatientRecord withSurgeries(@NotNull List<Surgery> surgeries) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .surgeries(surgeries)
                        .build())
                .build();
    }
}