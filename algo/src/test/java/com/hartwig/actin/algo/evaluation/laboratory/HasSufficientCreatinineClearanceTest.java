package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSufficientCreatinineClearanceTest {

    private static final double EPSILON = 1.0E-2;

    private static final Map<CreatinineClearanceMethod, LabMeasurement> TEST_CLEARANCE_MAP = Maps.newHashMap();

    static {
        TEST_CLEARANCE_MAP.put(CreatinineClearanceMethod.EGFR_CKD_EPI, LabMeasurement.EGFR_CKD_EPI);
        TEST_CLEARANCE_MAP.put(CreatinineClearanceMethod.EGFR_MDRD, LabMeasurement.EGFR_MDRD);
        TEST_CLEARANCE_MAP.put(CreatinineClearanceMethod.COCKCROFT_GAULT, LabMeasurement.CREATININE_CLEARANCE_CG);
    }

    @Test
    public void canEvaluateAllDirectClearances() {
        for (CreatinineClearanceMethod method : CreatinineClearanceMethod.values()) {
            HasSufficientCreatinineClearance function = new HasSufficientCreatinineClearance(2021, method, 4D);

            assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

            ImmutableLabValue.Builder lab = LaboratoryTestUtil.builder().code(TEST_CLEARANCE_MAP.get(method).code());

            assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(lab.value(6D).build())));
            assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(lab.value(2D).build())));

            // Fail when only creatinine is passed but not according to expected unit.
            ImmutableLabValue.Builder creatinine = LaboratoryTestUtil.builder().code(LabMeasurement.CREATININE.code()).unit("not a unit");
            assertEquals(Evaluation.UNDETERMINED, function.evaluate(LaboratoryTestUtil.withLabValue(creatinine.value(6D).build())));
        }
    }

    @Test
    public void canEvaluateMDRD() {
        HasSufficientCreatinineClearance function = new HasSufficientCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100D);

        LabValue creatinine = LaboratoryTestUtil.builder()
                .code(LabMeasurement.CREATININE.code())
                .unit(HasSufficientCreatinineClearance.EXPECTED_CREATININE_UNIT)
                .value(70D)
                .build();

        PatientRecord male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> maleValues = function.toMDRD(male, creatinine);
        assertEquals(103.54, maleValues.get(0), EPSILON);
        assertEquals(125.49, maleValues.get(1), EPSILON);

        assertEquals(Evaluation.PASS, function.evaluate(male));

        PatientRecord female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> femaleValues = function.toMDRD(female, creatinine);
        assertEquals(76.83, femaleValues.get(0), EPSILON);
        assertEquals(93.11, femaleValues.get(1), EPSILON);

        assertEquals(Evaluation.FAIL, function.evaluate(female));
    }

    @Test
    public void canEvaluateCKDEPI() {
        HasSufficientCreatinineClearance function =
                new HasSufficientCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100D);

        LabValue creatinine = LaboratoryTestUtil.builder()
                .code(LabMeasurement.CREATININE.code())
                .unit(HasSufficientCreatinineClearance.EXPECTED_CREATININE_UNIT)
                .value(70D)
                .build();

        PatientRecord male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> maleValues = function.toCKDEPI(male, creatinine);
        assertEquals(104.62, maleValues.get(0), EPSILON);
        assertEquals(121.25, maleValues.get(1), EPSILON);

        assertEquals(Evaluation.PASS, function.evaluate(male));

        PatientRecord female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        List<Double> femaleValues = function.toCKDEPI(female, creatinine);
        assertEquals(87.07, femaleValues.get(0), EPSILON);
        assertEquals(100.91, femaleValues.get(1), EPSILON);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(female));
    }

    @Test
    public void canEvaluateCockcroftGaultWithWeight() {
        HasSufficientCreatinineClearance function =
                new HasSufficientCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100D);

        LabValue creatinine = LaboratoryTestUtil.builder()
                .code(LabMeasurement.CREATININE.code())
                .unit(HasSufficientCreatinineClearance.EXPECTED_CREATININE_UNIT)
                .value(70D)
                .build();

        List<BodyWeight> weights = Lists.newArrayList();
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2020, 1, 1)).value(50D).unit(Strings.EMPTY).build());
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 1, 1)).value(60D).unit(Strings.EMPTY).build());

        PatientRecord maleLight = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.FAIL, function.evaluate(maleLight));

        PatientRecord femaleLight = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.FAIL, function.evaluate(femaleLight));

        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 2, 2)).value(70D).unit(Strings.EMPTY).build());

        PatientRecord maleHeavy = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.PASS, function.evaluate(maleHeavy));

        PatientRecord femaleHeavy = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights);
        assertEquals(Evaluation.FAIL, function.evaluate(femaleHeavy));
    }

    @Test
    public void canEvaluateCockcroftGaultNoWeight() {
        HasSufficientCreatinineClearance function =
                new HasSufficientCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100D);

        LabValue creatinine = LaboratoryTestUtil.builder()
                .code(LabMeasurement.CREATININE.code())
                .unit(HasSufficientCreatinineClearance.EXPECTED_CREATININE_UNIT)
                .value(70D)
                .build();

        LabValue ckdepiHigh = LaboratoryTestUtil.builder().code(LabMeasurement.EGFR_CKD_EPI.code()).value(100D).build();
        PatientRecord fallBack1 = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine, ckdepiHigh), Lists.newArrayList());
        assertEquals(Evaluation.PASS_BUT_WARN, function.evaluate(fallBack1));

        LabValue ckdepiLow = LaboratoryTestUtil.builder().code(LabMeasurement.EGFR_CKD_EPI.code()).value(10D).build();
        PatientRecord fallBack2 = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine, ckdepiLow), Lists.newArrayList());
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(fallBack2));

        PatientRecord fallBack3 = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(fallBack3));
    }

    @NotNull
    public static PatientRecord create(int birthYear, @NotNull Gender gender, @NotNull List<LabValue> labValues,
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