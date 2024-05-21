package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord()
private const val TUMOR_LOCATION = "tumorLocation"

private const val TUMOR_SUB_LOCATION = "tumorSubLocation"

private const val TUMOR_TYPE = "tumorType"

private const val TUMOR_SUB_TYPE = "tumorSubType"

private const val DOID = "3910"

private const val CONCLUSION_1 = "conclusion 1"

private const val CONCLUSION_2 = "conclusion 2"

private const val CONCLUSION_3 = "conclusion 3"

private const val CONCLUSION_4 = "conclusion 4"

private const val RADIOLOGY_REPORT = "Prior to conclusion:\n" +
        "Conclusie:\n" +
        "$CONCLUSION_1.\n" +
        "$CONCLUSION_2.\n" +
        "\r\n\n\nHersenen radiologie rapport:\n" +
        "Conclusie:\n" +
        "$CONCLUSION_3.\n" +
        "$CONCLUSION_4.\n" +
        "\r\n\n\nanother radiologie rapport:\n"

private val TUMOR_DETAILS = TumorDetails(
    primaryTumorLocation = TUMOR_LOCATION,
    primaryTumorSubLocation = TUMOR_SUB_LOCATION,
    primaryTumorType = TUMOR_TYPE,
    primaryTumorSubType = TUMOR_SUB_TYPE,
    doids = setOf(DOID),
    stage = TumorStage.IV,
    hasMeasurableDisease = true,
    hasBrainLesions = false,
    brainLesionsCount = 0,
    hasActiveBrainLesions = null,
    hasCnsLesions = false,
    cnsLesionsCount = 0,
    hasActiveCnsLesions = null,
    hasBoneLesions = false,
    boneLesionsCount = 0,
    hasLiverLesions = false,
    liverLesionsCount = 0,
    hasLungLesions = false,
    lungLesionsCount = 0,
    hasLymphNodeLesions = false,
    lymphNodeLesionsCount = 0,
    otherLesions = emptyList(),
    biopsyLocation = null
)
private val CURATION_CONFIG = PrimaryTumorConfig(
    input = "$TUMOR_LOCATION | $TUMOR_TYPE",
    ignore = false,
    primaryTumorLocation = TUMOR_LOCATION,
    primaryTumorType = TUMOR_TYPE,
    doids = setOf(DOID),
    primaryTumorExtraDetails = "tumorExtraDetails",
    primaryTumorSubType = TUMOR_SUB_TYPE,
    primaryTumorSubLocation = TUMOR_SUB_LOCATION,
)

private val UNUSED_DATE = LocalDate.of(2024, 4, 10)

private val EHR_PRIOR_OTHER_CONDITION = ProvidedPriorOtherCondition(
    name = PRIOR_CONDITION_INPUT,
    startDate = UNUSED_DATE
)

private val BRAIN_LESION_LOCATION_CONFIG = LesionLocationConfig(
    input = PRIOR_CONDITION_INPUT,
    location = "brain",
    category = LesionLocationCategory.BRAIN
)

private val LUNG_LESION_LOCATION_CONFIG = BRAIN_LESION_LOCATION_CONFIG.copy(location = "lung", category = LesionLocationCategory.LUNG)

private val BRAIN_AND_LUNG_LESION_TUMOR_DETAILS = TUMOR_DETAILS.copy(
    hasBrainLesions = true,
    brainLesionsCount = 1,
    hasLungLesions = true,
    lungLesionsCount = 1
)

class ProvidedTumorDetailsExtractorTest {

    private val tumorCuration = mockk<CurationDatabase<PrimaryTumorConfig>>()
    private val lesionCuration = mockk<CurationDatabase<LesionLocationConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val tumorStageDeriver = mockk<TumorStageDeriver> {
        every { derive(any()) } returns null
    }
    private val extractor = ProvidedTumorDetailsExtractor(tumorCuration, lesionCuration, tumorStageDeriver)

    @Test
    fun `Should curate primary tumor and extract tumor details, only drawing on curation for (sub)location, (sub)type and doids`() {
        every { tumorCuration.find("tumorLocation | tumorType") } returns setOf(CURATION_CONFIG)
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS)
    }

    @Test
    fun `Should extract tumor details from only the EHR when no curation found`() {
        every { tumorCuration.find("tumorLocation | tumorType") } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                primaryTumorSubType = null,
                primaryTumorSubLocation = null,
                doids = emptySet()
            )
        )
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                EHR_PATIENT_RECORD.patientDetails.hashedId,
                CurationCategory.PRIMARY_TUMOR,
                "tumorLocation | tumorType",
                "Could not find primary tumor config for input 'tumorLocation | tumorType'",
            )
        )
    }

    @Test
    fun `Should not attempt to derive stages when no curation found`() {
        every { tumorCuration.find("tumorLocation | tumorType") } returns emptySet()
        extractor.extract(EHR_PATIENT_RECORD)
        verify(exactly = 0) { tumorStageDeriver.derive(any()) }
    }

    @Test
    fun `Should call deriver to derive stages when tumor details have been curated`() {
        every { tumorCuration.find("tumorLocation | tumorType") } returns setOf(CURATION_CONFIG)
        every { tumorStageDeriver.derive(any()) } returns setOf(TumorStage.II)
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted.derivedStages).isEqualTo(setOf(TumorStage.II))
    }

    @Test
    fun `Should curate lesions from the radiology report in lesion site`() {
        setupLesionCuration(
            CONCLUSION_1, BRAIN_LESION_LOCATION_CONFIG
        )
        every { lesionCuration.find(CONCLUSION_2) } returns setOf(
            LesionLocationConfig(
                input = CONCLUSION_2,
                location = "other",
                category = null
            )
        )
        every { lesionCuration.find(CONCLUSION_3) } returns emptySet()
        every { lesionCuration.find(CONCLUSION_4) } returns setOf(
            LesionLocationConfig(
                input = CONCLUSION_4,
                ignore = true,
                location = "other",
                category = null
            )
        )
        val result =
            extractor.extract(EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(lesionSite = RADIOLOGY_REPORT)))
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                brainLesionsCount = 1,
                otherLesions = listOf("other")
            )
        )
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                EHR_PATIENT_RECORD.patientDetails.hashedId,
                CurationCategory.LESION_LOCATION,
                CONCLUSION_3,
                "Could not find lesion config for input 'conclusion 3'",
            )
        )
    }

    @Test
    fun `Should curate lesions from prior conditions, supporting multiple configs per input, but ignore any curation warnings`() {
        setupLesionCuration(
            PRIOR_CONDITION_INPUT, BRAIN_LESION_LOCATION_CONFIG, LUNG_LESION_LOCATION_CONFIG
        )
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    priorOtherConditions = listOf(
                        EHR_PRIOR_OTHER_CONDITION,
                        EHR_PRIOR_OTHER_CONDITION.copy(name = "another prior condition")
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(BRAIN_AND_LUNG_LESION_TUMOR_DETAILS)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.evaluation.lesionLocationEvaluatedInputs).containsExactly(PRIOR_CONDITION_INPUT)
    }

    @Test
    fun `Should curate lesions from treatment history, supporting multiple configs per input, but ignore any curation warnings`() {
        setupLesionCuration(
            TREATMENT_HISTORY_INPUT,
            BRAIN_LESION_LOCATION_CONFIG,
            LUNG_LESION_LOCATION_CONFIG
        )
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    treatmentHistory = listOf(
                        EhrTestData.createEhrTreatmentHistory().copy(treatmentName = TREATMENT_HISTORY_INPUT),
                        EhrTestData.createEhrTreatmentHistory().copy(treatmentName = "another treatment history")
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(BRAIN_AND_LUNG_LESION_TUMOR_DETAILS)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.evaluation.lesionLocationEvaluatedInputs).containsExactly(TREATMENT_HISTORY_INPUT)
    }

    private fun setupLesionCuration(input: String, vararg lesionLocationConfig: LesionLocationConfig) {
        every { tumorCuration.find("tumorLocation | tumorType") } returns setOf(CURATION_CONFIG)
        every { lesionCuration.find(input) } returns lesionLocationConfig.toSet()
    }
}