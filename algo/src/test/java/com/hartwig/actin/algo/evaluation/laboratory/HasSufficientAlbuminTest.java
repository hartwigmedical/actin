package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientAlbuminTest {

    @Test
    public void canEvaluate() {
        HasSufficientAlbumin function = new HasSufficientAlbumin(3D);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder albumin = LabTestFactory.forMeasurement(LabMeasurement.ALBUMIN);

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(record, albumin.value(20D).unit(LabUnit.GRAMS_PER_LITER).build()));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, albumin.value(20D).unit(LabUnit.GRAMS_PER_DECILITER).build()));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, albumin.value(40D).unit(LabUnit.GRAMS_PER_LITER).build()));
    }
}