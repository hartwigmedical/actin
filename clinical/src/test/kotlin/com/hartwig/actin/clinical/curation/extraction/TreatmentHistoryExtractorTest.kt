package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory.emptyQuestionnaire
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class TreatmentHistoryExtractorTest {

    @Test
    fun shouldCurateTreatmentHistory() {
        val questionnaire = emptyQuestionnaire().copy(
            treatmentHistoryCurrentTumor = listOf("Cis 2020 2021", "no systemic treatment"),
            otherOncologicalHistory = listOf(CANNOT_CURATE)
        )

        val (treatmentHistory, evaluation) = TreatmentHistoryExtractor(TestCurationFactory.createProperTestCurationDatabase()).extract(
            PATIENT_ID,
            questionnaire
        )
        assertThat(treatmentHistory).hasSize(2)
        assertThat(treatmentHistory).anyMatch { 2020 == it.startYear() }
        assertThat(treatmentHistory).anyMatch { 2021 == it.startYear() }

        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.ONCOLOGICAL_HISTORY,
                CANNOT_CURATE,
                "Could not find treatment history or second primary config for input '$CANNOT_CURATE'"
            )
        )
    }
}