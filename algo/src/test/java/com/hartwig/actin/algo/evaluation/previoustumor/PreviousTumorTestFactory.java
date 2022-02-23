package com.hartwig.actin.algo.evaluation.previoustumor;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;

final class PreviousTumorTestFactory {

    private PreviousTumorTestFactory() {
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
