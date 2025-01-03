package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WarnIfTest {

    @Test
    fun `Should warn if underlying function evaluates to pass or warn`() {
        val patient: PatientRecord = TestPatientFactory.createProperTestPatientRecord()
        assertEvaluation(EvaluationResult.WARN, WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(patient))
        assertEvaluation(EvaluationResult.WARN, WarnIf(TestEvaluationFunctionFactory.warn()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.fail()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.undetermined()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient))
    }

    @Test
    fun `Should move messages to warn on pass`() {
        val result: Evaluation =
            WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertThat(result.passSpecificMessages).isEmpty()
        assertThat(result.passGeneralMessages).isEmpty()
        assertThat(result.warnSpecificMessages).isNotEmpty()
        assertThat(result.warnGeneralMessages).isNotEmpty()
    }

    @Test
    fun `Should not return inclusion or exclusion molecular events`(){
        val result: Evaluation =
            WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertThat(result.inclusionMolecularEvents).isEmpty()
        assertThat(result.exclusionMolecularEvents).isEmpty()
    }
}