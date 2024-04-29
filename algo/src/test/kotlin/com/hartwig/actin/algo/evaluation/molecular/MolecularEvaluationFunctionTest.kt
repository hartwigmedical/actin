package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.archerPriorMolecularNoFusionsFoundRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val OVERRIDE_MESSAGE = "Override message"

class MolecularEvaluationFunctionTest {
    private val function = object : MolecularEvaluationFunction {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.fail("Fail specific message", "Fail general message")
        }
    }

    private val functionWithOverride = object : MolecularEvaluationFunction {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.pass("OK")
        }

        override fun noMolecularRecordEvaluation() = EvaluationFactory.fail(OVERRIDE_MESSAGE)
    }

    @Test
    fun `Should return no molecular data message when no ORANGE nor other molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("No molecular data")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("No molecular data")
    }

    @Test
    fun `Should return insufficient molecular data when no ORANGE but other molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistory.fromInputs(emptyList(), listOf(archerPriorMolecularNoFusionsFoundRecord())))
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("Insufficient molecular data")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("Insufficient molecular data")
    }

    @Test
    fun `Should execute rule when ORANGE molecular data`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).containsExactly("Fail specific message")
        assertThat(evaluation.failGeneralMessages).containsExactly("Fail general message")
    }

    @Test
    fun `Should use override message when provided for patient with no molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        assertOverrideEvaluation(patient)
    }

    @Test
    fun `Should use override message when provided for patient with no ORANGE record but other data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistory.fromInputs(emptyList(), listOf(archerPriorMolecularNoFusionsFoundRecord())))
        assertOverrideEvaluation(patient)
    }

    private fun assertOverrideEvaluation(patient: PatientRecord) {
        val evaluation = functionWithOverride.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).containsExactly(OVERRIDE_MESSAGE)
        assertThat(evaluation.failGeneralMessages).containsExactly(OVERRIDE_MESSAGE)
    }
}