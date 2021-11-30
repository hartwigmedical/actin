package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientCreatinineClearanceTest {

    private static final Map<CreatinineClearanceMethod, LabMeasurement> CLEARANCE_MAP = Maps.newHashMap();

    static {
        CLEARANCE_MAP.put(CreatinineClearanceMethod.EGFR_CDK_EPI, LabMeasurement.EGFR_CDK_EPI);
        CLEARANCE_MAP.put(CreatinineClearanceMethod.EGFR_MDRD, LabMeasurement.EGFR_MDRD);
        CLEARANCE_MAP.put(CreatinineClearanceMethod.COCKCROFT_GAULT, LabMeasurement.CREATININE_CLEARANCE_CG);
    }

    @Test
    public void canEvaluateAllDirectClearances() {
        for (CreatinineClearanceMethod method : CreatinineClearanceMethod.values()) {
            HasSufficientCreatinineClearance function = new HasSufficientCreatinineClearance(method, 4D);

            assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

            ImmutableLabValue.Builder lab = LaboratoryTestUtil.builder().code(CLEARANCE_MAP.get(method).code());

            assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(lab.value(6D).build())));
            assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(lab.value(2D).build())));
        }
    }
}