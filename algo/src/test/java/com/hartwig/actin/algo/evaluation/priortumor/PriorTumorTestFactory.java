package com.hartwig.actin.algo.evaluation.priortumor;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class PriorTumorTestFactory {

    private PriorTumorTestFactory() {
    }

    @NotNull
    public static ImmutablePriorSecondPrimary.Builder builder() {
        return ImmutablePriorSecondPrimary.builder()
                .tumorLocation(Strings.EMPTY)
                .tumorSubLocation(Strings.EMPTY)
                .tumorType(Strings.EMPTY)
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .isActive(false);
    }

    @NotNull
    public static PatientRecord withPriorSecondPrimary(@NotNull PriorSecondPrimary priorSecondPrimary) {
        return withPriorSecondPrimaries(Lists.newArrayList(priorSecondPrimary));
    }

    @NotNull
    public static PatientRecord withPriorSecondPrimaries(@NotNull List<PriorSecondPrimary> priorSecondPrimaries) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorSecondPrimaries(priorSecondPrimaries)
                        .build())
                .build();
    }
}
