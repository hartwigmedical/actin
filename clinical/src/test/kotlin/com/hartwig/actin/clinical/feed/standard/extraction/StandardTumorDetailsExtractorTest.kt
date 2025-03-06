package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
import com.hartwig.actin.clinical.feed.standard.TREATMENT_HISTORY_INPUT
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.provided.ProvidedLesion
import com.hartwig.actin.datamodel.clinical.provided.ProvidedOtherCondition
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

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
        "$CONCLUSION_1\r\n" +
        "$CONCLUSION_2.\n" +
        "\r\n\n\nHersenen radiologie rapport:\n" +
        "Conclusie:\n" +
        "$CONCLUSION_3\r\n" +
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
    hasActiveBrainLesions = false,
    hasCnsLesions = false,
    cnsLesionsCount = 0,
    hasActiveCnsLesions = false,
    hasBoneLesions = false,
    boneLesionsCount = 0,
    hasLiverLesions = false,
    liverLesionsCount = 0,
    hasLungLesions = false,
    lungLesionsCount = 0,
    hasLymphNodeLesions = false,
    lymphNodeLesionsCount = 0,
    otherLesions = emptyList(),
    otherSuspectedLesions = emptyList(),
    biopsyLocation = null
)

private val TUMOR_INPUT = "$TUMOR_LOCATION | $TUMOR_TYPE"

private val TUMOR_CURATION_CONFIG = PrimaryTumorConfig(
    input = TUMOR_INPUT,
    ignore = false,
    primaryTumorLocation = TUMOR_LOCATION,
    primaryTumorType = TUMOR_TYPE,
    doids = setOf(DOID),
    primaryTumorExtraDetails = "tumorExtraDetails",
    primaryTumorSubType = TUMOR_SUB_TYPE,
    primaryTumorSubLocation = TUMOR_SUB_LOCATION,
)

private val UNUSED_DATE = LocalDate.of(2024, 4, 10)

private val EHR_PRIOR_OTHER_CONDITION = ProvidedOtherCondition(
    name = OTHER_CONDITION_INPUT,
    startDate = UNUSED_DATE
)

private val BRAIN_LESION_LOCATION_CONFIG = LesionLocationConfig(
    input = OTHER_CONDITION_INPUT,
    location = "brain",
    category = LesionLocationCategory.BRAIN
)

private val BRAIN_LESION_SUSPECTED_LOCATION_CONFIG = BRAIN_LESION_LOCATION_CONFIG.copy(
    suspected = true,
)

private val LUNG_LESION_LOCATION_CONFIG = BRAIN_LESION_LOCATION_CONFIG.copy(location = "lung", category = LesionLocationCategory.LUNG)

private val BRAIN_AND_LUNG_LESION_TUMOR_DETAILS = TUMOR_DETAILS.copy(
    hasBrainLesions = true,
    brainLesionsCount = 1,
    hasLungLesions = true,
    lungLesionsCount = 1
)

private val DIAGNOSIS_DATE = LocalDate.of(2024, 8, 1)

class StandardTumorDetailsExtractorTest {

    private val tumorCuration = mockk<CurationDatabase<PrimaryTumorConfig>> {
        every { find(OTHER_CONDITION_INPUT) } returns emptySet()
    }
    private val lesionCuration = mockk<CurationDatabase<LesionLocationConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val tumorStageDeriver = mockk<TumorStageDeriver> {
        every { derive(any()) } returns null
    }
    private val extractor = StandardTumorDetailsExtractor(tumorCuration, lesionCuration, tumorStageDeriver)

    @Test
    fun `Should curate primary tumor and extract tumor details, only drawing on curation for (sub)location, (sub)type and doids`() {
        every { tumorCuration.find("tumorLocation | tumorType") } returns setOf(TUMOR_CURATION_CONFIG)
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS)
    }

    @Test
    fun `Should curate other conditions and extract tumor details, only drawing on curation for (sub)location, (sub)type and doids`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG.copy(ignore = true))
        setupTumorCuration(OTHER_CONDITION_INPUT, TUMOR_CURATION_CONFIG)
        val result = extractor.extract(EHR_PATIENT_RECORD.copy(priorOtherConditions = listOf(EHR_PRIOR_OTHER_CONDITION)))
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS)
    }

    @Test
    fun `Should extract tumor details from only the EHR when no curation found`() {
        setupTumorCuration(TUMOR_INPUT)
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
        setupTumorCuration(TUMOR_INPUT)
        extractor.extract(EHR_PATIENT_RECORD)
        verify(exactly = 0) { tumorStageDeriver.derive(any()) }
    }

    @Test
    fun `Should call deriver to derive stages when tumor details have been curated`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        every { tumorStageDeriver.derive(any()) } returns setOf(TumorStage.II)
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted.derivedStages).isEqualTo(setOf(TumorStage.II))
    }

    @Test
    fun `Should curate lesions from the radiology report in lesion site`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration(CONCLUSION_1, BRAIN_LESION_LOCATION_CONFIG)
        setupLesionCuration(CONCLUSION_2, LesionLocationConfig(input = CONCLUSION_2, location = "other 2", category = null))
        setupLesionCuration(CONCLUSION_3)
        setupLesionCuration(CONCLUSION_4, LesionLocationConfig(input = CONCLUSION_4, location = "other 4", category = null, ignore = true))

        val result =
            extractor.extract(EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(lesionSite = RADIOLOGY_REPORT)))
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                brainLesionsCount = 1,
                otherLesions = listOf("other 2")
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
    fun `Should curate suspected lesions from the radiology report in lesion site`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration(CONCLUSION_1, BRAIN_LESION_SUSPECTED_LOCATION_CONFIG)
        setupLesionCuration(CONCLUSION_2, LesionLocationConfig(input = CONCLUSION_2, location = "other 2", category = null))
        setupLesionCuration(CONCLUSION_3)
        setupLesionCuration(CONCLUSION_4, LesionLocationConfig(input = CONCLUSION_4, location = "other 4", category = null, ignore = true))

        val result =
            extractor.extract(EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(lesionSite = RADIOLOGY_REPORT)))
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasSuspectedBrainLesions = true,
                brainLesionsCount = 1,
                otherLesions = listOf("other 2")
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
    fun `Should curate lesions and suspected lesions from the radiology report in lesion site`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration(CONCLUSION_1, BRAIN_LESION_LOCATION_CONFIG)
        setupLesionCuration(CONCLUSION_2, BRAIN_LESION_SUSPECTED_LOCATION_CONFIG.copy(input = CONCLUSION_2))

        val result =
            extractor.extract(EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(lesionSite = RADIOLOGY_REPORT)))
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                hasSuspectedBrainLesions = true,
                brainLesionsCount = 2
            )
        )
    }

    @Test
    fun `Should curate lesions from prior conditions, supporting multiple configs per input, but ignore any curation warnings`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration(OTHER_CONDITION_INPUT, BRAIN_LESION_LOCATION_CONFIG, LUNG_LESION_LOCATION_CONFIG)
        every { tumorCuration.find("another prior condition") } returns emptySet()
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
        assertThat(result.evaluation.lesionLocationEvaluatedInputs).containsExactly(OTHER_CONDITION_INPUT)
    }

    @Test
    fun `Should curate lesions from treatment history, supporting multiple configs per input, but ignore any curation warnings`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration(TREATMENT_HISTORY_INPUT, BRAIN_LESION_LOCATION_CONFIG, LUNG_LESION_LOCATION_CONFIG)
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

    @Test
    fun `Should extract lesions from provided tumor details lesion list, and not curate if location is a known category`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        lesions = listOf(
                            ProvidedLesion("brain", DIAGNOSIS_DATE, true)
                        )
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                hasActiveBrainLesions = true,
                brainLesionsCount = 1,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract lesions from provided tumor details lesion list, and curate if location is not a known category`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration("brainnnn", BRAIN_LESION_LOCATION_CONFIG)
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        lesions = listOf(
                            ProvidedLesion("brainnnn", DIAGNOSIS_DATE)
                        )
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                brainLesionsCount = 1,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract suspected lesions from provided tumor details lesion list, and curate if location is not a known category`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        setupLesionCuration("brainnnn suspected", BRAIN_LESION_SUSPECTED_LOCATION_CONFIG)
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        lesions = listOf(
                            ProvidedLesion("brainnnn suspected", DIAGNOSIS_DATE)
                        )
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasSuspectedBrainLesions = true,
                brainLesionsCount = 1,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract raw pathology report text from patient record if provided`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val base = EHR_PATIENT_RECORD
        val providedRecord = base.copy(tumorDetails = base.tumorDetails.copy(rawPathologyReport = "Some report"))
        val result = extractor.extract(providedRecord)
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS.copy(rawPathologyReport = "Some report"))
    }

    private fun setupTumorCuration(input: String, vararg primaryTumorConfig: PrimaryTumorConfig) {
        every { tumorCuration.find(input) } returns primaryTumorConfig.toSet()
    }

    private fun setupLesionCuration(input: String, vararg lesionLocationConfig: LesionLocationConfig) {
        every { lesionCuration.find(input) } returns lesionLocationConfig.toSet()
    }
}