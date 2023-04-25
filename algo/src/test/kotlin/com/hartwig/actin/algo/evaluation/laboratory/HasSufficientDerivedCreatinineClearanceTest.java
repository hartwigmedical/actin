package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSufficientDerivedCreatinineClearanceTest {

    @Test
    public void canEvaluateMDRD() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100D);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        // MDRD between 103 and 125
        PatientRecord male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(male, creatinine));

        // MDRD between 73 and 95
        PatientRecord female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(female, creatinine));
    }

    @Test
    public void canEvaluateCKDEPI() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100D);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        // CDK-EPI between 104 and 125
        PatientRecord male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(male, creatinine));

        // CDK-EPI between 87 and 101
        PatientRecord female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(female, creatinine));
    }

    @Test
    public void canEvaluateCockcroftGaultWithWeight() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100D);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        List<BodyWeight> weights = Lists.newArrayList();
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2020, 1, 1)).value(50D).unit(Strings.EMPTY).build());
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 1, 1)).value(60D).unit(Strings.EMPTY).build());

        // CG 95
        PatientRecord maleLight = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(maleLight, creatinine));

        // CG 80
        PatientRecord femaleLight = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(femaleLight, creatinine));

        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 2, 2)).value(70D).unit(Strings.EMPTY).build());

        // CG 111
        PatientRecord maleHeavy = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(maleHeavy, creatinine));

        // CG 94
        PatientRecord femaleHeavy = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(femaleHeavy, creatinine));
    }

    @Test
    public void canEvaluateCockcroftGaultNoWeight() {
        HasSufficientDerivedCreatinineClearance function =
                new HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 80);

        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        // CG 103
        PatientRecord fallBack1 = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEvaluation(EvaluationResult.WARN, function.evaluate(fallBack1, creatinine));

        // CG 67
        PatientRecord fallBack2 = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(fallBack2, creatinine));
    }

    @NotNull
    private static PatientRecord create(int birthYear, @NotNull Gender gender, @NotNull List<LabValue> labValues,
            @NotNull List<BodyWeight> bodyWeights) {
        ClinicalRecord base = TestClinicalFactory.createMinimalTestClinicalRecord();

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