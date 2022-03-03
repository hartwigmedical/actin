package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSpecificComplicationTest {

    @Test
    public void canEvaluate() {
        HasSpecificComplication function = new HasSpecificComplication("name to find");

        List<Complication> complications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withComplications(complications)));

        complications.add(ImmutableComplication.builder().name("just a name").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withComplications(complications)));

        complications.add(ImmutableComplication.builder().name("this includes name to find").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withComplications(complications)));
    }

    @NotNull
    private static PatientRecord withComplications(@NotNull List<Complication> complications) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .complications(complications)
                        .build())
                .build();
    }
}