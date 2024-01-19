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

private const val MOLECULAR_TEST_INTERPRETATION = "Molecular test interpretation"

class PriorMolecularTestsExtractorTest {

    val extractor = PriorMolecularTestsExtractor(
        TestCurationFactory.curationDatabase(
            MolecularTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = PriorMolecularTest(
                    impliesPotentialIndeterminateStatus = false, test = MOLECULAR_TEST_INTERPRETATION, item = "item"
                )
            )
        )
    )

    @Test
    fun `Should curate prior molecular tests`() {
        val inputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test).isEqualTo(MOLECULAR_TEST_INTERPRETATION)

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
}