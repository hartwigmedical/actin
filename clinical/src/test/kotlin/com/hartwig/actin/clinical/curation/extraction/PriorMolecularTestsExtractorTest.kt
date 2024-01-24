package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val MOLECULAR_TEST_INPUT = "Molecular test input"
private const val NEGATIVE_TEST_INPUT = "negative"

private const val MOLECULAR_TEST_INTERPRETATION = "Molecular test interpretation"

class PriorMolecularTestsExtractorTest {

    val extractor = PriorMolecularTestsExtractor(
        TestCurationFactory.curationDatabase(
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION).item("item").build()
            )
        )
    )

    @Test
    fun `Should curate prior molecular tests`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST,
                CANNOT_CURATE,
                "Could not find IHC molecular test config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    private val pdl1Extractor = PriorMolecularTestsExtractor(
        TestCurationFactory.curationDatabase(
            MolecularTestConfig(
                input = "PD-L1 negative",
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION).item("item").build()
            )
        )
    )

    @Test
    fun `Should curate negative input from PD-L1 field`() {
        val inputs = listOf(NEGATIVE_TEST_INPUT)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(pdl1TestResults = inputs)
        val (priorMolecularTests, evaluation) = pdl1Extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION)

        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should warn on negative input from IHC field`() {
        val inputs = listOf(NEGATIVE_TEST_INPUT)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = inputs)
        val (priorMolecularTests, evaluation) = pdl1Extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).isEmpty()

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST,
                NEGATIVE_TEST_INPUT,
                "Could not find IHC molecular test config for input '${NEGATIVE_TEST_INPUT}'"
            )
        )
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }
}