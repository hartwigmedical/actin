package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val NON_ONCOLOGICAL_INPUT = "Non-oncological input"
private const val CURATED_LVEF = 1.0

class ClinicalStatusExtractorTest {
    private val extractor = ClinicalStatusExtractor(
        TestCurationFactory.curationDatabase(
            ComorbidityConfig(input = NON_ONCOLOGICAL_INPUT, ignore = false, lvef = CURATED_LVEF, curated = null),
        )
    )

    @Test
    fun `Should return empty extraction when no questionnaire`() {
        val (clinicalStatus, evaluation) = extractor.extract(null, true)
        assertThat(clinicalStatus).isEqualTo(ClinicalStatus())
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should extract clinical status and curate infection, ecg and lvef`() {
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(whoStatus = 1, nonOncologicalHistory = listOf(NON_ONCOLOGICAL_INPUT))
        val (clinicalStatus, evaluation) = extractor.extract(questionnaire, true)
        assertClinicalStatus(clinicalStatus)
        assertThat(evaluation.warnings).isEmpty()
    }

    private fun assertClinicalStatus(clinicalStatus: ClinicalStatus) {
        assertThat(clinicalStatus.who).isEqualTo(1)
        assertThat(clinicalStatus.lvef).isEqualTo(CURATED_LVEF)
        assertThat(clinicalStatus.hasComplications).isTrue
    }
}