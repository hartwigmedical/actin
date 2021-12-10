package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class OtherConditionTestUtil {

    private OtherConditionTestUtil() {
    }

    @NotNull
    public static PatientRecord withPriorOtherCondition(@NotNull PriorOtherCondition priorOtherCondition) {
        return withPriorOtherConditions(Lists.newArrayList(priorOtherCondition));
    }

    @NotNull
    public static PatientRecord withPriorOtherConditions(@NotNull List<PriorOtherCondition> priorOtherConditions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorOtherConditions(priorOtherConditions)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutablePriorOtherCondition.Builder builder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY);
    }
}
