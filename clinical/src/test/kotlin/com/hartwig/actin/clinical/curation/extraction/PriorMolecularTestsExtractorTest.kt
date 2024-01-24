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
private const val MOLECULAR_TEST_INPUT_SPECIFIC = "Molecular test input specific"

private const val MOLECULAR_TEST_INTERPRETATION = "Molecular test interpretation"
private const val MOLECULAR_TEST_INTERPRETATION_SPECIFIC = "Molecular test interpretation specific"
private const val MOLECULAR_TEST_INTERPRETATION_FALLBACK = "Molecular test interpretation fallback"

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
                input = "PD-L1 $MOLECULAR_TEST_INPUT_SPECIFIC",
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION_SPECIFIC).item("item").build()
            ),
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT_SPECIFIC,
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION_FALLBACK).item("item").build()
            ),
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION).item("item").build()
            ),
        )
    )

    @Test
    fun `Should curate input from PD-L1 field when specific curation when available`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT_SPECIFIC)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(pdl1TestResults = inputs)
        val (priorMolecularTests, evaluation) = pdl1Extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION_SPECIFIC)

        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should curate input from PD-L1 field when no specific curation config available`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(pdl1TestResults = inputs)
        val (priorMolecularTests, evaluation) = pdl1Extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION)

        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should curate input from IHC field`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = inputs)
        val (priorMolecularTests, evaluation) = pdl1Extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION)

        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }
}