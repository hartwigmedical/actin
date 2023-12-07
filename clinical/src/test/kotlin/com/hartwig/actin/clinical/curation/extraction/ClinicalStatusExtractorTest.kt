package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val NO_CURATION_NEEDED = "No curation needed"

class ClinicalStatusExtractorTest {
    private val extractor = ClinicalStatusExtractor(mockk(), mockk(), mockk())

    @Test
    fun `Should extract clinical status`() {
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(
                whoStatus = 1,
                infectionStatus = toInfection("weird infection"),
                ecg = toECG("weird aberration"),
                nonOncologicalHistory = listOf("LVEF 0.17")
            )
        val (clinicalStatus, evaluation) = extractor.extract(PATIENT_ID, questionnaire, true)
        assertThat(clinicalStatus.who()).isEqualTo(1)
        assertThat(clinicalStatus.infectionStatus()?.hasActiveInfection()).isTrue()
        assertThat(clinicalStatus.infectionStatus()?.description()).isEqualTo("Cleaned infection")
        assertThat(clinicalStatus.ecg()?.hasSigAberrationLatestECG()).isTrue()
        assertThat(clinicalStatus.ecg()?.aberrationDescription()).isEqualTo("Cleaned aberration")
        assertThat(clinicalStatus.lvef()).isEqualTo(0.17)
        assertThat(clinicalStatus.hasComplications()).isTrue()
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate ECGs`() {
        extractAndAssertECG("Weird aberration", "Cleaned aberration")
        extractAndAssertECG(
            NO_CURATION_NEEDED,
            NO_CURATION_NEEDED,
            CurationWarning(
                PATIENT_ID, CurationCategory.ECG, NO_CURATION_NEEDED, "Could not find ECG config for input '$NO_CURATION_NEEDED'"
            )
        )
        extractAndAssertECG("Yes but unknown what aberration", null)
        assertThat(extractor.curateECG(PATIENT_ID, toECG("No aberration")).extracted).isNull()
        assertThat(extractor.curateECG(PATIENT_ID, null).extracted).isNull()
    }

    private fun extractAndAssertECG(
        inputDescription: String, expectedDescription: String?, expectedWarning: CurationWarning? = null
    ) {
        val (curatedECG, evaluation) = extractor.curateECG(PATIENT_ID, toECG(inputDescription))
        assertThat(curatedECG).isNotNull
        assertThat(curatedECG!!.aberrationDescription()).isEqualTo(expectedDescription)
        assertThat(evaluation.warnings).isEqualTo(setOfNotNull(expectedWarning))
        assertThat(evaluation.ecgEvaluatedInputs).isEqualTo(setOf(inputDescription.lowercase()))
    }

    @Test
    fun `Should curate infection status`() {
        extractAndAssertInfection("Cleaned infection", "Weird infection")
        extractAndAssertInfection(
            NO_CURATION_NEEDED, NO_CURATION_NEEDED, CurationWarning(
                PATIENT_ID,
                CurationCategory.INFECTION,
                NO_CURATION_NEEDED,
                "Could not find infection config for input '$NO_CURATION_NEEDED'"
            )
        )
        extractAndAssertInfection(null, "No Infection")
        assertThat(extractor.curateInfection(PATIENT_ID, null).extracted).isNull()
    }

    private fun extractAndAssertInfection(expected: String?, inputDescription: String, expectedWarning: CurationWarning? = null) {
        val (infectionStatus, evaluation) = extractor.curateInfection(PATIENT_ID, toInfection(inputDescription))
        assertThat(infectionStatus).isNotNull
        assertThat(infectionStatus!!.description()).isEqualTo(expected)
        assertThat(evaluation.warnings).isEqualTo(setOfNotNull(expectedWarning))
        assertThat(evaluation.infectionEvaluatedInputs).isEqualTo(setOf(inputDescription.lowercase()))
    }

    @Test
    fun `Should determine LVEF`() {
        assertThat(extractor.determineLVEF(null)).isNull()
        assertThat(extractor.determineLVEF(listOf("not an LVEF"))).isNull()
        assertThat(extractor.determineLVEF(listOf("LVEF 0.17"))).isNotNull.isEqualTo(0.17)
    }

    companion object {

        private fun toECG(aberrationDescription: String): ECG {
            return ImmutableECG.builder().hasSigAberrationLatestECG(true).aberrationDescription(aberrationDescription).build()
        }

        private fun toInfection(description: String): InfectionStatus {
            return ImmutableInfectionStatus.builder().hasActiveInfection(true).description(description).build()
        }
    }
}