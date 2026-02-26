package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val OVERRIDE_MESSAGE = "Override message"
private const val FAIL_MESSAGE = "Fail message"

class MolecularEvaluationFunctionTest {

    private val function = object : MolecularEvaluationFunction(useInsufficientQualityRecords = false) {
        override fun evaluate(test: MolecularTest): Evaluation {
            return EvaluationFactory.fail(FAIL_MESSAGE)
        }
    }

    private val functionWithOverride = object : MolecularEvaluationFunction(useInsufficientQualityRecords = false) {
        override fun evaluate(test: MolecularTest): Evaluation {
            return EvaluationFactory.pass("OK")
        }

        override fun noMolecularTestEvaluation() = EvaluationFactory.fail(OVERRIDE_MESSAGE)
    }

    private val functionWithGene = object : MolecularEvaluationFunction(gene = "GENE", useInsufficientQualityRecords = false) {}

    private val functionWithGenesAndTarget = object : MolecularEvaluationFunction(
        gene = "GENE",
        targetCoveragePredicate = specific(MolecularTestTarget.FUSION, messagePrefix = "Test in"),
        useInsufficientQualityRecords = false
    ) {}

    @Test
    fun `Should return no molecular results message when no ORANGE nor other molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("No molecular results of sufficient quality")
    }
    
    private fun emptyPanel(testDate: LocalDate? = null) =
        TestMolecularFactory.createMinimalPanelTest().copy(experimentType = ExperimentType.PANEL, date = testDate)

    @Test
    fun `Should execute rule when ORANGE molecular data`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val evaluation = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(FAIL_MESSAGE)
    }

    @Test
    fun `Should use override message when provided for patient with no molecular data`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
        assertOverrideEvaluation(patient)
    }
    
    @Test
    fun `Should return undetermined when genes have not been tested which are mandatory`() {
        val patient = withPanelTest()
        val evaluation = functionWithGene.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "gene GENE undetermined (not tested for mutations, amplifications, deletions or fusions)"
        )
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue()
    }

    @Test
    fun `Should return undetermined when mandatory genes and targets have not been tested`() {
        val patient = withPanelTest()
        val evaluation =
            functionWithGenesAndTarget.evaluate(
                patient.copy(
                    molecularTests =
                    listOf(
                        TestMolecularFactory.createMinimalPanelTest()
                            .copy(
                                targetSpecification = TestMolecularFactory.panelSpecifications(
                                    setOf("GENE"),
                                    listOf(MolecularTestTarget.MUTATION)
                                )
                            )
                    )
                )
            )
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Test in gene GENE undetermined (not tested for fusions)")
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue()
    }

    private fun withPanelTest() = TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(molecularTests = listOf(emptyPanel()))

    private fun assertOverrideEvaluation(patient: PatientRecord) {
        val evaluation = functionWithOverride.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(OVERRIDE_MESSAGE)
    }
}