package com.hartwig.actin.algo.evaluation.general;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class IsInvolvedInStudyProceduresTest {

    @Test
    public void canEvaluate() {
        IsInvolvedInStudyProcedures function = new IsInvolvedInStudyProcedures();

        assertEquals(Evaluation.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}