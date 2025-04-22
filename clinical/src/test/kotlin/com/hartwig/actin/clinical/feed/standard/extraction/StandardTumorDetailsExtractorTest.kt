package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
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
private const val TUMOR_INPUT = "$TUMOR_LOCATION | $TUMOR_TYPE"

private val TUMOR_DETAILS = TumorDetails(
    primaryTumorLocation = TUMOR_LOCATION,
    primaryTumorSubLocation = TUMOR_SUB_LOCATION,
    primaryTumorType = TUMOR_TYPE,
    primaryTumorSubType = TUMOR_SUB_TYPE,
    doids = setOf(DOID),
    stage = TumorStage.IV,
    derivedStages = null,
    hasMeasurableDisease = true,
    hasBrainLesions = false,
    hasActiveBrainLesions = false,
    hasSuspectedBrainLesions = null,
    brainLesionsCount = 0,
    hasCnsLesions = null,
    hasActiveCnsLesions = null,
    hasSuspectedCnsLesions = null,
    cnsLesionsCount = 0,
    hasBoneLesions = false,
    hasSuspectedBoneLesions = null,
    boneLesionsCount = 0,
    hasLiverLesions = false,
    hasSuspectedLiverLesions = null,
    liverLesionsCount = 0,
    hasLungLesions = null,
    hasSuspectedLungLesions = null,
    lungLesionsCount = null,
    hasLymphNodeLesions = null,
    lymphNodeLesionsCount = null,
    otherLesions = null,
    otherSuspectedLesions = null,
    biopsyLocation = null,
)

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
    fun `Should extract lesions from provided tumor details lesion list`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        lesions = listOf(
                            ProvidedLesion("liver", DIAGNOSIS_DATE, true)
                        )
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasLiverLesions = true,
                liverLesionsCount = 1,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate CNS lesions from brain lesions in case of inactive brain lesions`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        lesions = listOf(
                            ProvidedLesion("brain", DIAGNOSIS_DATE, false)
                        )
                    )
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                brainLesionsCount = 1,
                hasCnsLesions = true,
                cnsLesionsCount = 1
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate CNS lesions from brain lesions in case of active brain lesions`() {
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
                hasCnsLesions = true,
                hasActiveCnsLesions = true,
                cnsLesionsCount = 1
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
}