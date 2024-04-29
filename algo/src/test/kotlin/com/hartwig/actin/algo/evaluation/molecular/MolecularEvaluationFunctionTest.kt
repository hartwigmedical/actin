package com.hartwig.actin.algo.evaluation.molecular

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

class MolecularEvaluationFunctionTest {
    private val function = object : MolecularEvaluationFunction {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.fail("Fail specific message", "Fail general message")
        }
    }

    @Test
    fun `Should return no molecular data message when no Orange nor other molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("No molecular data")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("No molecular data")
    }

    @Test
    fun `Should return insufficient molecular data when no Orange but other molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistory.fromInputs(emptyList(), listOf(archerPriorMolecularNoFusionsFoundRecord())))
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("Insufficient molecular data")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("Insufficient molecular data")
    }

    @Test
    fun `Should execute rule when Orange molecular data`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).containsExactly("Fail specific message")
        assertThat(evaluation.failGeneralMessages).containsExactly("Fail general message")
    }
}