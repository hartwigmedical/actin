package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"

private const val ECG_INPUT = "ECG input"

private const val INFECTION_INPUT = "Infection input"

private const val NON_ONCOLOGICAL_INPUT = "Non-oncological input"

private const val CURATED_ECG = "Curated ECG"

private const val CURATED_INFECTION = "Curated Infection"

private const val CURATED_LVEF = 1.0

private const val CANNOT_CURATE = "Cannot curate"

class ClinicalStatusExtractorTest {
    private val extractor = ClinicalStatusExtractor(
        TestCurationFactory.curationDatabase(
            ECGConfig(
                input = ECG_INPUT,
                ignore = false,
                interpretation = CURATED_ECG,
                isQTCF = false,
                isJTC = false
            )
        ),
        TestCurationFactory.curationDatabase(InfectionConfig(input = INFECTION_INPUT, interpretation = CURATED_INFECTION)),
        TestCurationFactory.curationDatabase(
            NonOncologicalHistoryConfig(
                input = NON_ONCOLOGICAL_INPUT,
                ignore = false,
                lvef = CURATED_LVEF,
                priorOtherCondition = null
            )
        )
    )

    @Test
    fun `Should return empty extraction when no questionnaire`() {
        val (clinicalStatus, evaluation) = extractor.extract(PATIENT_ID, null, true)
        assertThat(clinicalStatus).isEqualTo(ImmutableClinicalStatus.builder().build())
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.ecgEvaluatedInputs).isEmpty()
        assertThat(evaluation.infectionEvaluatedInputs).isEmpty()
        assertThat(evaluation.nonOncologicalHistoryEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should extract clinical status and curate infection, ecg and lvef`() {
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(
                whoStatus = 1,
                infectionStatus = infectionStatus(INFECTION_INPUT),
                ecg = ecg(ECG_INPUT),
                nonOncologicalHistory = listOf(NON_ONCOLOGICAL_INPUT)
            )
        val (clinicalStatus, evaluation) = extractor.extract(PATIENT_ID, questionnaire, true)
        assertClinicalStatus(clinicalStatus)
        assertThat(evaluation.ecgEvaluatedInputs).containsOnly(ECG_INPUT.lowercase())
        assertThat(evaluation.infectionEvaluatedInputs).containsOnly(INFECTION_INPUT.lowercase())
        assertThat(evaluation.nonOncologicalHistoryEvaluatedInputs).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract clinical status with warnings when curation not possible`() {
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(
                whoStatus = 1,
                infectionStatus = infectionStatus(CANNOT_CURATE),
                ecg = ecg(CANNOT_CURATE),
                nonOncologicalHistory = listOf(NON_ONCOLOGICAL_INPUT)
            )
        val (clinicalStatus, evaluation) = extractor.extract(PATIENT_ID, questionnaire, true)
        assertThat(clinicalStatus).isNotNull
        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = PATIENT_ID,
                category = CurationCategory.ECG,
                feedInput = CANNOT_CURATE,
                message = "Could not find ECG config for input 'Cannot curate'"
            ),
            CurationWarning(
                patientId = PATIENT_ID,
                category = CurationCategory.INFECTION,
                feedInput = CANNOT_CURATE,
                message = "Could not find infection config for input 'Cannot curate'"
            )
        )
    }

    private fun ecg(input: String): ImmutableECG? =
        ImmutableECG.builder().aberrationDescription(input).hasSigAberrationLatestECG(true).build()

    private fun infectionStatus(input: String): ImmutableInfectionStatus? =
        ImmutableInfectionStatus.builder().description(input).hasActiveInfection(true).build()

    private fun assertClinicalStatus(clinicalStatus: ClinicalStatus) {
        assertThat(clinicalStatus.who()).isEqualTo(1)
        assertThat(clinicalStatus.infectionStatus()?.hasActiveInfection()).isTrue()
        assertThat(clinicalStatus.infectionStatus()?.description()).isEqualTo(CURATED_INFECTION)
        assertThat(clinicalStatus.ecg()?.hasSigAberrationLatestECG()).isTrue()
        assertThat(clinicalStatus.ecg()?.aberrationDescription()).isEqualTo(CURATED_ECG)
        assertThat(clinicalStatus.lvef()).isEqualTo(CURATED_LVEF)
        assertThat(clinicalStatus.hasComplications()).isTrue()
    }
}