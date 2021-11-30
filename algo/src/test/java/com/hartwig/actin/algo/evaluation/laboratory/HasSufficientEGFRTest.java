package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientEGFRTest {

    @Test
    public void canEvaluateDirectCDKEPI() {
        HasSufficientEGFR function = new HasSufficientEGFR(EGFRMethod.CDK_EPI, 4D);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder cdkEpi = LaboratoryTestUtil.builder().code(LabMeasurement.EGFR_CDK_EPI.code());

        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(cdkEpi.value(6D).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(cdkEpi.value(2D).build())));
    }

    @Test
    public void canEvaluateDirectMDRD() {
        HasSufficientEGFR function = new HasSufficientEGFR(EGFRMethod.MDRD, 4D);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder mdrd = LaboratoryTestUtil.builder().code(LabMeasurement.EGFR_MDRD.code());

        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(mdrd.value(6D).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(mdrd.value(2D).build())));
    }
}