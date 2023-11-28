package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory.emptyQuestionnaire
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class PriorSecondPrimaryExtractorTest {
    private val extractor = PriorSecondPrimaryExtractor(TestCurationFactory.createProperTestCurationDatabase())

    @Test
    fun `Should curate prior second primaries`() {
        val inputs = listOf("Breast cancer Jan-2018", CANNOT_CURATE)
        val questionnaire = emptyQuestionnaire().copy(secondaryPrimaries = inputs)
        val (priorSecondPrimaries, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorSecondPrimaries).hasSize(1)
        assertThat(priorSecondPrimaries[0].tumorLocation()).isEqualTo("Breast")

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.SECOND_PRIMARY,
                CANNOT_CURATE,
                "Could not find second primary or treatment history config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.secondPrimaryEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should extract empty list when questionnaire second primary list is null`() {
        assertThat(extractor.extract(PATIENT_ID, emptyQuestionnaire()).extracted).isEmpty()
    }
}