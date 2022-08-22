package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class TreatmentTestFactory {

    private TreatmentTestFactory() {
    }

    @NotNull
    public static ImmutablePriorTumorTreatment.Builder builder() {
        return ImmutablePriorTumorTreatment.builder().isSystemic(false).name(Strings.EMPTY);
    }

    @NotNull
    public static PatientRecord withPriorTumorTreatment(@NotNull PriorTumorTreatment priorTumorTreatment) {
        return withPriorTumorTreatments(Lists.newArrayList(priorTumorTreatment));
    }

    @NotNull
    public static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(priorTumorTreatments)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutablePriorSecondPrimary.Builder priorSecondPrimaryBuilder() {
        return ImmutablePriorSecondPrimary.builder()
                .tumorLocation(Strings.EMPTY)
                .tumorSubLocation(Strings.EMPTY)
                .tumorType(Strings.EMPTY)
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .isActive(false);
    }
}
