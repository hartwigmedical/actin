package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestPanelRecordFactory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val OVERRIDE_MESSAGE = "Override message"
private const val FAIL_SPECIFIC_MESSAGE = "Fail specific message"
private const val FAIL_GENERAL_MESSAGE = "Fail general message"
private val MAX_AGE = LocalDate.of(2023, 9, 6)

class MolecularEvaluationFunctionTest {
    private val function = object : MolecularEvaluationFunction(useInsufficientQualityRecords = false) {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.fail(FAIL_SPECIFIC_MESSAGE, FAIL_GENERAL_MESSAGE)
        }
    }

    private val functionWithOverride = object : MolecularEvaluationFunction(useInsufficientQualityRecords = false) {
        override fun evaluate(molecular: MolecularRecord): Evaluation {
            return EvaluationFactory.pass("OK")
        }

        override fun noMolecularRecordEvaluation() = EvaluationFactory.fail(OVERRIDE_MESSAGE)
    }

    private val functionOnMolecularHistory = object : MolecularEvaluationFunction(useInsufficientQualityRecords = false) {
        override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
            return EvaluationFactory.fail(
                FAIL_SPECIFIC_MESSAGE,
                FAIL_GENERAL_MESSAGE
            )
        }
    }

    private val functionWithGenes = object : MolecularEvaluationFunction(useInsufficientQualityRecords = false) {
        override fun genes(): List<String> {
            return listOf("GENE")
        }
    }

    @Test
    fun `Should return no molecular results message when no ORANGE nor other molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("No molecular results of sufficient quality")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("No molecular results of sufficient quality")
    }

    @Test
    fun `Should return insufficient molecular data when no ORANGE but other molecular data`() {
        val patient = withPanelTest()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("Insufficient molecular data")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("Insufficient molecular data")
    }

    private fun emptyArcher(testDate: LocalDate? = null) =
        TestPanelRecordFactory.empty().copy(experimentType = ExperimentType.PANEL, date = testDate)

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
        val patient = withPanelTest()
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

    @Test
    fun `Should return undetermined when genes have not been tested which are mandatory`() {
        val patient = withPanelTest()
        val evaluation = functionWithGenes.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("Gene(s) GENE not tested")
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("Gene(s) GENE not tested in molecular data")
        assertThat(evaluation.isMissingGenesForSufficientEvaluation).isTrue()
    }

    @Test
    fun `Should only evaluate tests under max age when specified`() {
        val evaluatedTests = mutableSetOf<MolecularTest>()
        val function = object : MolecularEvaluationFunction(MAX_AGE, false) {
            override fun evaluate(test: MolecularTest): Evaluation {
                evaluatedTests.add(test)
                return EvaluationFactory.fail(FAIL_SPECIFIC_MESSAGE, FAIL_GENERAL_MESSAGE)
            }
        }
        val newTest = MAX_AGE.plusDays(1)
        val oldTest = MAX_AGE.minusDays(1)
        val patient = withPanelTest(newTest, oldTest)
        function.evaluate(patient)
        assertThat(evaluatedTests.map { it.date }).containsOnly(newTest)
    }

    private fun withPanelTest(vararg testDates: LocalDate = arrayOf(MAX_AGE.plusYears(1))) =
        TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistory(testDates.map { emptyArcher(it) }))

    private fun assertOverrideEvaluation(patient: PatientRecord) {
        val evaluation = functionWithOverride.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).containsExactly(OVERRIDE_MESSAGE)
        assertThat(evaluation.failGeneralMessages).containsExactly(OVERRIDE_MESSAGE)
    }
}