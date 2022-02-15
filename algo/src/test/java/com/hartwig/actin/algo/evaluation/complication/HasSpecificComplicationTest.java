package com.hartwig.actin.algo.evaluation.complication;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSpecificComplicationTest {

    @Test
    public void canEvaluate() {
        HasSpecificComplication function = new HasSpecificComplication("name to find");

        List<CancerRelatedComplication> complications = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL, function.evaluate(withCancerRelatedComplications(complications)));

        complications.add(ImmutableCancerRelatedComplication.builder().name("just a name").build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withCancerRelatedComplications(complications)));

        complications.add(ImmutableCancerRelatedComplication.builder().name("this includes name to find").build());
        assertEquals(EvaluationResult.PASS, function.evaluate(withCancerRelatedComplications(complications)));
    }

    @NotNull
    private static PatientRecord withCancerRelatedComplications(@NotNull List<CancerRelatedComplication> complications) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .cancerRelatedComplications(complications)
                        .build())
                .build();
    }

}