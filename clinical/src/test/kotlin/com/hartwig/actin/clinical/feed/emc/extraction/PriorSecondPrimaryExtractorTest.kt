package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory.emptyQuestionnaire
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TumorStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val SECOND_PRIMARY_INPUT = "Second primary input"

private const val TUMOR_LOCATION_INTERPRETATION = "Tumor location interpretation"

private const val TREATMENT_HISTORY_INPUT = "Treatment history input"

class PriorSecondPrimaryExtractorTest {
    private val extractor = PriorSecondPrimaryExtractor(
        TestCurationFactory.curationDatabase(
            SecondPrimaryConfig(
                input = SECOND_PRIMARY_INPUT,
                ignore = false,
                curated = PriorSecondPrimary(
                    status = TumorStatus.ACTIVE,
                    tumorType = "tumorType",
                    tumorSubType = "tumorSubType",
                    treatmentHistory = "treatmentHistory",
                    tumorLocation = TUMOR_LOCATION_INTERPRETATION,
                    tumorSubLocation = "tumorSubLocation"
                )
            )
        ),
        TestCurationFactory.curationDatabase(
            TreatmentHistoryEntryConfig(
                input = TREATMENT_HISTORY_INPUT, ignore = false, curated = TreatmentTestFactory.treatmentHistoryEntry()
            )
        )
    )

    @Test
    fun `Should curate prior second primaries from second primary config`() {
        val inputs = listOf(SECOND_PRIMARY_INPUT, CANNOT_CURATE)
        val questionnaire = emptyQuestionnaire().copy(secondaryPrimaries = inputs)
        val (priorSecondPrimaries, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorSecondPrimaries).hasSize(1)
        assertThat(priorSecondPrimaries[0].tumorLocation).isEqualTo(TUMOR_LOCATION_INTERPRETATION)

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
    fun `Should suppress warnings when prior second primaries from treatment history`() {
        val inputs = listOf(TREATMENT_HISTORY_INPUT)
        val questionnaire = emptyQuestionnaire().copy(secondaryPrimaries = inputs)
        val (_, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract empty list when questionnaire second primary list is null`() {
        assertThat(extractor.extract(PATIENT_ID, emptyQuestionnaire()).extracted).isEmpty()
    }
}