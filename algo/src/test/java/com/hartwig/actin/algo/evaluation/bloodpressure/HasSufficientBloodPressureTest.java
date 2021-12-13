package com.hartwig.actin.algo.evaluation.bloodpressure;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.BloodPressure;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodPressure;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSufficientBloodPressureTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2020, 11, 19);

    @Test
    public void canEvaluate() {
        BloodPressureCategory category = BloodPressureCategory.SYSTOLIC;
        HasSufficientBloodPressure function = new HasSufficientBloodPressure(category, 100);
        List<BloodPressure> bloodPressures = Lists.newArrayList();

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(withBloodPressures(bloodPressures)));

        bloodPressures.add(builder().date(REFERENCE_DATE).category(category.display()).value(110).build());
        assertEquals(Evaluation.PASS, function.evaluate(withBloodPressures(bloodPressures)));

        // Fail when the average falls below 100
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(1)).category(category.display()).value(70).build());
        assertEquals(Evaluation.FAIL, function.evaluate(withBloodPressures(bloodPressures)));

        // Succeed again when the average goes above 100
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(2)).category(category.display()).value(110).build());
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(3)).category(category.display()).value(110).build());
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(4)).category(category.display()).value(110).build());
        assertEquals(Evaluation.PASS, function.evaluate(withBloodPressures(bloodPressures)));

        // Still succeed since we only take X most recent measures.
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(5)).category(category.display()).value(20).build());
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(6)).category(category.display()).value(20).build());
        bloodPressures.add(builder().date(REFERENCE_DATE.minusDays(7)).category(category.display()).value(20).build());
        assertEquals(Evaluation.PASS, function.evaluate(withBloodPressures(bloodPressures)));
    }

    @Test
    public void canFilterOnCategory() {
        HasSufficientBloodPressure function = new HasSufficientBloodPressure(BloodPressureCategory.SYSTOLIC, 100);
        List<BloodPressure> bloodPressures = Lists.newArrayList();

        bloodPressures.add(builder().date(REFERENCE_DATE).category(BloodPressureCategory.DIASTOLIC.display()).value(110).build());
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(withBloodPressures(bloodPressures)));
    }

    @NotNull
    private static PatientRecord withBloodPressures(@NotNull List<BloodPressure> bloodPressures) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .bloodPressures(bloodPressures)
                        .build())
                .build();
    }

    @NotNull
    private static ImmutableBloodPressure.Builder builder() {
        return ImmutableBloodPressure.builder().date(LocalDate.of(2017, 7, 7)).category(Strings.EMPTY).value(0D).unit(Strings.EMPTY);
    }
}