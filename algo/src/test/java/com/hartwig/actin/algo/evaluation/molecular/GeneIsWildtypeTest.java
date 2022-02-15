package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsWildtypeTest {

    @Test
    public void canEvaluate() {
        GeneIsWildtype function = new GeneIsWildtype("gene 1");

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        //        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
        //
        //        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withWildtypeGene("gene 2")));
        //        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withWildtypeGene("gene 1")));
    }
}