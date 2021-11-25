package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.jetbrains.annotations.NotNull;

final class TumorEvaluationTestUtil {

    private TumorEvaluationTestUtil() {
    }

    @NotNull
    public static PatientRecord withTumorDetails(@NotNull TumorDetails tumor) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder().from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .tumor(tumor)
                        .build())
                .build();
    }
}
