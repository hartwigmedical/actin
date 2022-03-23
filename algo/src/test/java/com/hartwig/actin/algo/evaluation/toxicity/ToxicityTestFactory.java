package com.hartwig.actin.algo.evaluation.toxicity;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class ToxicityTestFactory {

    private ToxicityTestFactory() {
    }

    @NotNull
    public static PatientRecord withToxicities(@NotNull List<Toxicity> toxicities) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .toxicities(toxicities)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutableToxicity.Builder toxicity() {
        return ImmutableToxicity.builder().name(Strings.EMPTY).evaluatedDate(LocalDate.of(2020, 1, 1)).source(ToxicitySource.EHR);
    }

    @NotNull
    public static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> treatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(treatments)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutablePriorTumorTreatment.Builder treatment() {
        return ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(false);
    }

    @NotNull
    public static PatientRecord withIntolerance(@NotNull Intolerance intolerance) {
        return withIntolerances(Lists.newArrayList(intolerance));
    }

    @NotNull
    public static PatientRecord withIntolerances(@NotNull List<Intolerance> intolerances) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .intolerances(intolerances)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutableIntolerance.Builder intolerance() {
        return ImmutableIntolerance.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .type(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY);
    }
}
