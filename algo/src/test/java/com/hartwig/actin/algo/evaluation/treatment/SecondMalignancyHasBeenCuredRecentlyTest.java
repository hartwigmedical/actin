package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SecondMalignancyHasBeenCuredRecentlyTest {

    @Test
    public void canEvaluate() {
        SecondMalignancyHasBeenCuredRecently function = new SecondMalignancyHasBeenCuredRecently();

        assertEquals(Evaluation.PASS, function.evaluate(withSecondPrimaries(Lists.newArrayList())));

        PriorSecondPrimary secondPrimaryInactive = ImmutablePriorSecondPrimary.builder()
                .tumorLocation("Skin")
                .tumorSubLocation(Strings.EMPTY)
                .tumorType("Melanoma")
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .isActive(false)
                .build();

        assertEquals(Evaluation.PASS, function.evaluate(withSecondPrimaries(Lists.newArrayList(secondPrimaryInactive))));

        PriorSecondPrimary secondPrimaryActive = ImmutablePriorSecondPrimary.builder().from(secondPrimaryInactive).isActive(true).build();

        assertEquals(Evaluation.FAIL,
                function.evaluate(withSecondPrimaries(Lists.newArrayList(secondPrimaryInactive, secondPrimaryActive))));
    }

    @NotNull
    private static PatientRecord withSecondPrimaries(@NotNull List<PriorSecondPrimary> priorSecondPrimaries) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorSecondPrimaries(priorSecondPrimaries)
                        .build())
                .build();
    }
}