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

private const val MOLECULAR_TEST_INTERPRETATION_IHC = "Molecular test interpretation IHC"
private const val MOLECULAR_TEST_INTERPRETATION_PDL1 = "Molecular test interpretation PD-L1"


class PriorMolecularTestsExtractorTest {

    val extractor = PriorMolecularTestsExtractor(
        TestCurationFactory.curationDatabase(
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION_IHC).item("item").build()
            )
        ),
        TestCurationFactory.curationDatabase(
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder().impliesPotentialIndeterminateStatus(false)
                    .test(MOLECULAR_TEST_INTERPRETATION_PDL1).item("item").build()
            )
        ),
    )

    @Test
    fun `Should curate prior molecular IHC tests`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION_IHC)

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

    @Test
    fun `Should curate prior molecular PD-L1 tests`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(pdl1TestResults = inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION_PDL1)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST,
                CANNOT_CURATE,
                "Could not find PD-L1 molecular test config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should differentiate IHC and PD-L1 tests with same input`() {
        val ihcInputs = listOf(MOLECULAR_TEST_INPUT)
        val pdl1Inputs = listOf(MOLECULAR_TEST_INPUT)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = ihcInputs, pdl1TestResults = pdl1Inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(2)
        assertThat(priorMolecularTests[0].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION_IHC)
        assertThat(priorMolecularTests[1].test()).isEqualTo(MOLECULAR_TEST_INTERPRETATION_PDL1)

        assertThat(evaluation.warnings).isEmpty()
    }
}