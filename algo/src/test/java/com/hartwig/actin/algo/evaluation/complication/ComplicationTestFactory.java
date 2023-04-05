package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ComplicationTestFactory {

    private ComplicationTestFactory() {
    }

    @NotNull
    public static ImmutableComplication.Builder builder() {
        return ImmutableComplication.builder().name(Strings.EMPTY);
    }

    @NotNull
    public static PatientRecord withComplication(@NotNull Complication complication) {
        return withComplications(Lists.newArrayList(complication));
    }

    @NotNull
    public static PatientRecord withComplications(@Nullable List<Complication> complications) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base.clinical())
                        .complications(complications)
                        .clinicalStatus(ImmutableClinicalStatus.builder()
                                .hasComplications(Optional.ofNullable(complications).map(c -> !c.isEmpty()).orElse(null))
                                .build())
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withMedication(@NotNull Medication medication) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).medications(Lists.newArrayList(medication)).build())
                .build();
    }

    @NotNull
    public static PatientRecord withCnsLesion(@NotNull String lesion) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base.clinical())
                        .tumor(ImmutableTumorDetails.builder()
                                .from(base.clinical().tumor())
                                .hasCnsLesions(true)
                                .addOtherLesions(lesion)
                                .build())
                        .build())
                .build();
    }
}
