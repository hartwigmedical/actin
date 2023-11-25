package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class PriorMolecularTestsExtractorTest {

    @Test
    fun `Should curate prior molecular tests`() {
        val extractor = PriorMolecularTestsExtractor(TestCurationFactory.createProperTestCurationDatabase())
        val inputs = listOf("IHC ERBB2 3+", CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = inputs)
        val (priorMolecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorMolecularTests).hasSize(1)
        assertThat(priorMolecularTests[0].test()).isEqualTo("IHC")

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