package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val OVERRIDE_MESSAGE = "Override message"
private const val FAIL_SPECIFIC_MESSAGE = "Fail specific message"
private const val FAIL_GENERAL_MESSAGE = "Fail general message"

class MolecularEvaluationFunctionTest {
    private val function = object : MolecularEvaluationFunction {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.fail(FAIL_SPECIFIC_MESSAGE, FAIL_GENERAL_MESSAGE)
        }
    }

    private val functionWithOverride = object : MolecularEvaluationFunction {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.pass("OK")
        }

        override fun noMolecularRecordEvaluation() = EvaluationFactory.fail(OVERRIDE_MESSAGE)
    }

    private val functionOnMolecularHistory = object : MolecularEvaluationFunction {
        override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
            return EvaluationFactory.fail(
                FAIL_SPECIFIC_MESSAGE,
                FAIL_GENERAL_MESSAGE
            )
        }
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
            .copy(molecularHistory = MolecularHistory(listOf(ArcherPanel())))
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
        assertThat(evaluation.failSpecificMessages).containsExactly(FAIL_SPECIFIC_MESSAGE)
        assertThat(evaluation.failGeneralMessages).containsExactly(FAIL_GENERAL_MESSAGE)
    }

    @Test
    fun `Should use override message when provided for patient with no molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        assertOverrideEvaluation(patient)
    }

    @Test
    fun `Should use override message when provided for patient with no ORANGE record but other data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistory(listOf(ArcherPanel())))
        assertOverrideEvaluation(patient)
    }

    @Test
    fun `Should evaluate molecular history when available`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val evaluation = functionOnMolecularHistory.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).containsExactly(FAIL_SPECIFIC_MESSAGE)
        assertThat(evaluation.failGeneralMessages).containsExactly(FAIL_GENERAL_MESSAGE)
    }

    private fun assertOverrideEvaluation(patient: PatientRecord) {
        val evaluation = functionWithOverride.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).containsExactly(OVERRIDE_MESSAGE)
        assertThat(evaluation.failGeneralMessages).containsExactly(OVERRIDE_MESSAGE)
    }
}