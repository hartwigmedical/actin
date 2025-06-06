package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.DatedEntry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val TUMOR_LOCATION = "tumorLocation"
private const val TUMOR_SUB_LOCATION = "tumorSubLocation"
private const val TUMOR_TYPE = "tumorType"
private const val TUMOR_SUB_TYPE = "tumorSubType"
private const val DOID = "3910"
private const val TUMOR_INPUT = "$TUMOR_LOCATION | $TUMOR_TYPE"

private val TUMOR_DETAILS = TumorDetails(
    name = "name",
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
    hasCnsLesions = null,
    hasActiveCnsLesions = null,
    hasSuspectedCnsLesions = null,
    hasBoneLesions = false,
    hasSuspectedBoneLesions = null,
    hasLiverLesions = false,
    hasSuspectedLiverLesions = null,
    hasLungLesions = null,
    hasSuspectedLungLesions = null,
    hasLymphNodeLesions = null,
    otherLesions = null,
    otherSuspectedLesions = null,
    biopsyLocation = null,
)

private val TUMOR_CURATION_CONFIG = PrimaryTumorConfig(
    input = TUMOR_INPUT,
    ignore = false,
    name = "name",
    primaryTumorLocation = TUMOR_LOCATION,
    primaryTumorType = TUMOR_TYPE,
    doids = setOf(DOID),
    primaryTumorExtraDetails = "tumorExtraDetails",
    primaryTumorSubType = TUMOR_SUB_TYPE,
    primaryTumorSubLocation = TUMOR_SUB_LOCATION,
)

private val UNUSED_DATE = LocalDate.of(2024, 4, 10)

private val EHR_PRIOR_OTHER_CONDITION = DatedEntry(
    name = OTHER_CONDITION_INPUT,
    startDate = UNUSED_DATE
)

class StandardTumorDetailsExtractorTest {

    private val tumorCuration = mockk<CurationDatabase<PrimaryTumorConfig>> {
        every { find(OTHER_CONDITION_INPUT) } returns emptySet()
    }
    private val tumorStageDeriver = mockk<TumorStageDeriver> {
        every { derive(any()) } returns null
    }
    private val extractor = StandardTumorDetailsExtractor(tumorCuration, tumorStageDeriver)

    @Test
    fun `Should curate primary tumor and extract tumor details, only drawing on curation for (sub)location, (sub)type and doids`() {
        every { tumorCuration.find("tumorLocation | tumorType") } returns setOf(TUMOR_CURATION_CONFIG)
        val result = extractor.extract(FEED_PATIENT_RECORD)
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS)
    }

    @Test
    fun `Should curate other conditions and extract tumor details, only drawing on curation for (sub)location, (sub)type and doids`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG.copy(ignore = true))
        setupTumorCuration(OTHER_CONDITION_INPUT, TUMOR_CURATION_CONFIG)
        val result = extractor.extract(FEED_PATIENT_RECORD.copy(otherConditions = listOf(EHR_PRIOR_OTHER_CONDITION)))
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS)
    }

    @Test
    fun `Should extract tumor details from only the EHR when no curation found`() {
        setupTumorCuration(TUMOR_INPUT)
        val result = extractor.extract(FEED_PATIENT_RECORD)
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                name = "",
                primaryTumorSubType = null,
                primaryTumorSubLocation = null,
                doids = emptySet()
            )
        )
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                FEED_PATIENT_RECORD.patientDetails.patientId,
                CurationCategory.PRIMARY_TUMOR,
                "tumorLocation | tumorType",
                "Could not find primary tumor config for input 'tumorLocation | tumorType'",
            )
        )
    }

    @Test
    fun `Should not attempt to derive stages when no curation found`() {
        setupTumorCuration(TUMOR_INPUT)
        extractor.extract(FEED_PATIENT_RECORD)
        verify(exactly = 0) { tumorStageDeriver.derive(any()) }
    }

    @Test
    fun `Should call deriver to derive stages when tumor details have been curated`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        every { tumorStageDeriver.derive(any()) } returns setOf(TumorStage.II)
        val result = extractor.extract(FEED_PATIENT_RECORD)
        assertThat(result.extracted.derivedStages).isEqualTo(setOf(TumorStage.II))
    }

    @Test
    fun `Should assign lesions from feed tumor details`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val result =
            extractor.extract(
                FEED_PATIENT_RECORD.copy(
                    tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(hasLiverLesions = true)
                )
            )
        assertThat(result.extracted).isEqualTo(TUMOR_DETAILS.copy(hasLiverLesions = true))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate CNS lesions from brain lesions in case of inactive brain lesions`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val result =
            extractor.extract(
                FEED_PATIENT_RECORD.copy(
                    tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(hasBrainLesions = true)
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                hasCnsLesions = true
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate CNS lesions from brain lesions in case of active brain lesions`() {
        setupTumorCuration(TUMOR_INPUT, TUMOR_CURATION_CONFIG)
        val result =
            extractor.extract(
                FEED_PATIENT_RECORD.copy(
                    tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(hasBrainLesions = true, hasActiveBrainLesions = true)
                )
            )
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                hasBrainLesions = true,
                hasActiveBrainLesions = true,
                hasCnsLesions = true,
                hasActiveCnsLesions = true,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should set (active) brain and (active) CNS lesions to false in case of primary brain tumor`() {
        val tumorCuration = mockk<CurationDatabase<PrimaryTumorConfig>> {
            every { find("tumorLocation | Glioma") } returns setOf(
                PrimaryTumorConfig(
                    input = "tumorLocation | Glioma",
                    name = "name",
                    doids = setOf(DOID),
                    primaryTumorLocation = TUMOR_LOCATION,
                    primaryTumorSubLocation = TUMOR_SUB_LOCATION,
                    primaryTumorType = "Glioma",
                    primaryTumorSubType = TUMOR_SUB_TYPE,
                    ignore = false,
                    primaryTumorExtraDetails = "tumorExtraDetails"
                )
            )
        }
        val extractor = StandardTumorDetailsExtractor(tumorCuration, tumorStageDeriver)
        val result =
            extractor.extract(FEED_PATIENT_RECORD.copy(tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(tumorType = "Glioma")))
        assertThat(result.extracted).isEqualTo(
            TUMOR_DETAILS.copy(
                primaryTumorType = "Glioma",
                hasBrainLesions = false,
                hasActiveBrainLesions = false,
                hasCnsLesions = false,
                hasActiveCnsLesions = false,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    private fun setupTumorCuration(input: String, vararg primaryTumorConfig: PrimaryTumorConfig) {
        every { tumorCuration.find(input) } returns primaryTumorConfig.toSet()
    }
}