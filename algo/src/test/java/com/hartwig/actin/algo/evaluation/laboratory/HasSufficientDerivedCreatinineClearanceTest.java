package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSufficientDerivedCreatinineClearanceTest {

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canEvaluateMDRD() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100D);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        PatientRecord male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> maleValues = function.toMDRD(male, creatinine);
        assertEquals(103.54, maleValues.get(0), EPSILON);
        assertEquals(125.49, maleValues.get(1), EPSILON);

        assertEquals(Evaluation.PASS, function.evaluate(male, creatinine));

        PatientRecord female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> femaleValues = function.toMDRD(female, creatinine);
        assertEquals(76.83, femaleValues.get(0), EPSILON);
        assertEquals(93.11, femaleValues.get(1), EPSILON);

        assertEquals(Evaluation.FAIL, function.evaluate(female, creatinine));
    }

    @Test
    public void canEvaluateCKDEPI() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100D);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        PatientRecord male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> maleValues = function.toCKDEPI(male, creatinine);
        assertEquals(104.62, maleValues.get(0), EPSILON);
        assertEquals(121.25, maleValues.get(1), EPSILON);

        assertEquals(Evaluation.PASS, function.evaluate(male, creatinine));

        PatientRecord female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> femaleValues = function.toCKDEPI(female, creatinine);
        assertEquals(87.07, femaleValues.get(0), EPSILON);
        assertEquals(100.91, femaleValues.get(1), EPSILON);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(female, creatinine));
    }

    @Test
    public void canEvaluateCockcroftGaultWithWeight() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100D);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        List<BodyWeight> weights = Lists.newArrayList();
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2020, 1, 1)).value(50D).unit(Strings.EMPTY).build());
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 1, 1)).value(60D).unit(Strings.EMPTY).build());

        PatientRecord maleLight = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.FAIL, function.evaluate(maleLight, creatinine));

        PatientRecord femaleLight = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.FAIL, function.evaluate(femaleLight, creatinine));

        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 2, 2)).value(70D).unit(Strings.EMPTY).build());

        PatientRecord maleHeavy = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.PASS, function.evaluate(maleHeavy, creatinine));

        PatientRecord femaleHeavy = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.FAIL, function.evaluate(femaleHeavy, creatinine));
    }

    @Test
    public void canEvaluateCockcroftGaultNoWeight() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 80);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        PatientRecord fallBack1 = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEquals(Evaluation.PASS_BUT_WARN, function.evaluate(fallBack1, creatinine));

        PatientRecord fallBack2 = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(fallBack2, creatinine));
    }

    @NotNull
    private static PatientRecord create(int birthYear, @NotNull Gender gender, @NotNull List<LabValue> labValues,
            @NotNull List<BodyWeight> bodyWeights) {
        ClinicalRecord base = TestClinicalDataFactory.createMinimalTestClinicalRecord();

        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base)
                        .patient(ImmutablePatientDetails.builder().from(base.patient()).birthYear(birthYear).gender(gender).build())
                        .labValues(labValues)
                        .bodyWeights(bodyWeights)
                        .build())
                .build();
    }
}