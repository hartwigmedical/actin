package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasLimitedCreatinineTest {

    @Test
    public void canEvaluate() {
        HasLimitedCreatinine function = new HasLimitedCreatinine(1);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder creatinineMgPerDL =
                LabTestFactory.forMeasurement(LabMeasurement.CREATININE).unit(LabUnit.MILLIGRAMS_PER_DECILITER);
        ImmutableLabValue.Builder creatinineUMolPerL =
                LabTestFactory.forMeasurement(LabMeasurement.CREATININE).unit(LabUnit.MICROMOLES_PER_LITER);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, creatinineMgPerDL.value(2).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, creatinineMgPerDL.value(0.5).build()));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, creatinineUMolPerL.value(80).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, creatinineUMolPerL.value(120).build()));
    }
}