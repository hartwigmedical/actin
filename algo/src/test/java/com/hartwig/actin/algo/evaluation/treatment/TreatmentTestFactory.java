package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

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
    public static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(priorTumorTreatments)
                        .build())
                .build();
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
