package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;

final class ComplicationTestFactory {

    private ComplicationTestFactory() {
    }

    @NotNull
    public static PatientRecord withComplication(@NotNull Complication complication) {
        return withComplications(Lists.newArrayList(complication));
    }

    @NotNull
    public static PatientRecord withComplications(@NotNull List<Complication> complications) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).complications(complications).build())
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