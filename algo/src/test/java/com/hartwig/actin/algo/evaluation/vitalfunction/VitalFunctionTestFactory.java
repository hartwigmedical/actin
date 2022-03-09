package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.VitalFunction;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class VitalFunctionTestFactory {

    private VitalFunctionTestFactory() {
    }

    @NotNull
    public static PatientRecord withBodyWeights(@NotNull List<BodyWeight> bodyWeights) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .bodyWeights(bodyWeights)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutableBodyWeight.Builder bodyWeight() {
        return ImmutableBodyWeight.builder().date(LocalDate.of(2017, 7, 7)).value(0D).unit(Strings.EMPTY);
    }

    @NotNull
    public static PatientRecord withVitalFunctions(@NotNull List<VitalFunction> vitalFunctions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .vitalFunctions(vitalFunctions)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutableVitalFunction.Builder vitalFunction() {
        return ImmutableVitalFunction.builder().date(LocalDate.of(2017, 7, 7)).subcategory(Strings.EMPTY).value(0D).unit(Strings.EMPTY);
    }
}
