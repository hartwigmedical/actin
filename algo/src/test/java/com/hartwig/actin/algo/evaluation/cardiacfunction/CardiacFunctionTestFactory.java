package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CardiacFunctionTestFactory {

    private CardiacFunctionTestFactory() {
    }

    @NotNull
    public static ImmutableECG.Builder builder() {
        return ImmutableECG.builder().hasSigAberrationLatestECG(false).aberrationDescription(Strings.EMPTY);
    }

    @NotNull
    public static PatientRecord withHasSignificantECGAberration(boolean hasSignificantECGAberration) {
        return withECG(builder().hasSigAberrationLatestECG(hasSignificantECGAberration).build());
    }

    @NotNull
    public static PatientRecord withECG(@Nullable ECG ecg) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder().ecg(ecg).build())
                        .build())
                .build();
    }
}
