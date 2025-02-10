package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.algo.icd.IcdConstants.UNSPECIFIED_INFECTION_CODE
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.datamodel.clinical.OtherCondition
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
        assertThat(extractor.extract(null, null, true)).isEqualTo(ClinicalStatus())
    }

    @Test
    fun `Should extract clinical status and curate lvef`() {
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(whoStatus = 1, nonOncologicalHistory = listOf(NON_ONCOLOGICAL_INPUT), infectionStatus = InfectionStatus(true, "raw"))
        val extractedInfection = OtherCondition("Infection", setOf(IcdCode(UNSPECIFIED_INFECTION_CODE)))
        val clinicalStatus = extractor.extract(questionnaire, extractedInfection, true)
        assertThat(clinicalStatus.who).isEqualTo(1)
        assertThat(clinicalStatus.lvef).isEqualTo(CURATED_LVEF)
        assertThat(clinicalStatus.infectionStatus).isEqualTo(InfectionStatus(true, "Infection"))
        assertThat(clinicalStatus.hasComplications).isTrue
    }

}