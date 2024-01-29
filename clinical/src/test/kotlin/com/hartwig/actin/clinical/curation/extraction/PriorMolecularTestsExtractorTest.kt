package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
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
                curated = PriorMolecularTest(
                    impliesPotentialIndeterminateStatus = false, test = MOLECULAR_TEST_INTERPRETATION_IHC, item = "item"
                )
            ),
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = PriorMolecularTest(
                    impliesPotentialIndeterminateStatus = false, test = MOLECULAR_TEST_INTERPRETATION_PDL1, item = "item"
                )
            ),
        )
    )

    @Test
    fun `Should curate prior molecular tests`() {
        val ihcInputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)
        val pdl1Inputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)

        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = ihcInputs, pdl1TestResults = pdl1Inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(2)
        assertThat(priorMolecularTests[0].test).isEqualTo(MOLECULAR_TEST_INTERPRETATION_IHC)
        assertThat(priorMolecularTests[1].test).isEqualTo(MOLECULAR_TEST_INTERPRETATION_PDL1)

        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST,
                CANNOT_CURATE,
                "Could not find IHC molecular test config for input '$CANNOT_CURATE'"
            ),
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST,
                CANNOT_CURATE,
                "Could not find PD-L1 molecular test config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo((ihcInputs + pdl1Inputs).map(String::lowercase).toSet())
    }
}