package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import org.junit.Assert

object EvaluationAssert {
    fun assertMolecularEvaluation(expected: EvaluationResult, actual: Evaluation) {
        assertEvaluation(expected, actual)
        if (actual.result() == EvaluationResult.PASS || actual.result() == EvaluationResult.WARN) {
            Assert.assertFalse(actual.inclusionMolecularEvents().isEmpty())
        } else {
            Assert.assertTrue(actual.inclusionMolecularEvents().isEmpty())
        }
    }

    fun assertEvaluation(expected: EvaluationResult, actual: Evaluation) {
        Assert.assertEquals(expected, actual.result())
        if (actual.result() == EvaluationResult.PASS) {
            Assert.assertFalse(actual.passSpecificMessages().isEmpty())
            Assert.assertTrue(actual.warnSpecificMessages().isEmpty())
            Assert.assertTrue(actual.warnGeneralMessages().isEmpty())
            Assert.assertTrue(actual.undeterminedSpecificMessages().isEmpty())
            Assert.assertTrue(actual.undeterminedGeneralMessages().isEmpty())
            Assert.assertTrue(actual.failSpecificMessages().isEmpty())
            Assert.assertTrue(actual.failGeneralMessages().isEmpty())
        } else if (actual.result() == EvaluationResult.WARN) {
            Assert.assertTrue(actual.passSpecificMessages().isEmpty())
            Assert.assertTrue(actual.passGeneralMessages().isEmpty())
            Assert.assertFalse(actual.warnSpecificMessages().isEmpty())
            Assert.assertTrue(actual.undeterminedSpecificMessages().isEmpty())
            Assert.assertTrue(actual.undeterminedGeneralMessages().isEmpty())
            Assert.assertTrue(actual.failSpecificMessages().isEmpty())
            Assert.assertTrue(actual.failGeneralMessages().isEmpty())
        } else if (actual.result() == EvaluationResult.UNDETERMINED) {
            Assert.assertTrue(actual.passSpecificMessages().isEmpty())
            Assert.assertTrue(actual.passSpecificMessages().isEmpty())
            Assert.assertTrue(actual.passGeneralMessages().isEmpty())
            Assert.assertTrue(actual.warnSpecificMessages().isEmpty())
            Assert.assertTrue(actual.warnGeneralMessages().isEmpty())
            Assert.assertFalse(actual.undeterminedSpecificMessages().isEmpty())
            Assert.assertTrue(actual.failSpecificMessages().isEmpty())
            Assert.assertTrue(actual.failGeneralMessages().isEmpty())
        } else if (actual.result() == EvaluationResult.FAIL) {
            Assert.assertTrue(actual.passSpecificMessages().isEmpty())
            Assert.assertTrue(actual.passGeneralMessages().isEmpty())
            Assert.assertTrue(actual.warnSpecificMessages().isEmpty())
            Assert.assertTrue(actual.warnGeneralMessages().isEmpty())
            Assert.assertTrue(actual.undeterminedSpecificMessages().isEmpty())
            Assert.assertTrue(actual.undeterminedGeneralMessages().isEmpty())
            Assert.assertFalse(actual.failSpecificMessages().isEmpty())
        }
    }
}