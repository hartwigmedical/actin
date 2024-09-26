package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory.emptyQuestionnaire
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val SECOND_PRIMARY_INPUT = "Second primary input"

private const val TREATMENT_HISTORY_INPUT = "Treatment history input"

private const val CURATED_TREATMENT_NAME = "Curated treatment name"

class OncologicalHistoryExtractorTest {

    private val extractor = OncologicalHistoryExtractor(
        TestCurationFactory.curationDatabase(
            TreatmentHistoryEntryConfig(
                input = TREATMENT_HISTORY_INPUT,
                ignore = false,
                curated = treatmentHistoryEntry(setOf(drugTreatment(CURATED_TREATMENT_NAME, TreatmentCategory.CHEMOTHERAPY)))
            )
        ), TestCurationFactory.curationDatabase(
            SecondPrimaryConfig(
                input = SECOND_PRIMARY_INPUT,
                ignore = false,
                curated = null
            )
        )
    )

    @Test
    fun `Should extract and curate treatment history`() {
        val questionnaire = emptyQuestionnaire().copy(
            treatmentHistoryCurrentTumor = listOf(TREATMENT_HISTORY_INPUT),
            otherOncologicalHistory = listOf(CANNOT_CURATE)
        )

        val (treatmentHistory, evaluation) = extractor.extract(
            PATIENT_ID,
            questionnaire
        )
        assertThat(treatmentHistory).hasSize(1)
        assertThat(treatmentHistory[0].treatmentName()).isEqualTo(CURATED_TREATMENT_NAME)

        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.ONCOLOGICAL_HISTORY,
                CANNOT_CURATE,
                "Could not find treatment history or second primary config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.treatmentHistoryEntryEvaluatedInputs).isEqualTo(
            setOf(TREATMENT_HISTORY_INPUT.lowercase(), CANNOT_CURATE.lowercase())
        )
    }

    @Test
    fun `Should suppress warnings when prior second primaries from treatment history`() {
        val inputs = listOf(SECOND_PRIMARY_INPUT)
        val questionnaire = emptyQuestionnaire().copy(secondaryPrimaries = inputs)
        val (_, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(evaluation.warnings).isEmpty()
    }
}