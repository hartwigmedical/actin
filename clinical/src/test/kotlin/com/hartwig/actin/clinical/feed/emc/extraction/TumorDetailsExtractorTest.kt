package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.FeedLesion
import com.hartwig.feed.datamodel.FeedTumorDetail
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val TUMOR_LOCATION_INPUT = "Tumor location input"
private const val TUMOR_TYPE_INPUT = "Tumor type input"
private const val BIOPSY_LOCATION_INPUT = "Biopsy location input"

class TumorDetailsExtractorTest {

    private val baseTumor = TumorDetails()

    private val tumorStageDeriver = mockk<TumorStageDeriver> {
        every { derive(any()) } returns null
    }

    @Test
    fun `Should curate tumor with location only`() {
        val (curatedWithoutType, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(
                PrimaryTumorConfig(
                    input = "$TUMOR_LOCATION_INPUT |",
                    ignore = false,
                    name = "",
                    doids = emptySet()
                )
            ),
            tumorStageDeriver
        ).curateTumorDetails(PATIENT_ID, TUMOR_LOCATION_INPUT, null)
        assertThat(curatedWithoutType.name).isEqualTo("")
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate tumor with type only`() {
        val (curatedWithoutLocation, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(
                PrimaryTumorConfig(
                    input = "| $TUMOR_TYPE_INPUT",
                    ignore = false,
                    name = "name",
                    doids = emptySet()
                )
            ),
            tumorStageDeriver
        ).curateTumorDetails(PATIENT_ID, null, TUMOR_TYPE_INPUT)
        assertThat(curatedWithoutLocation.name).isEqualTo("name")
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should null tumor that does not exist`() {
        val (missing, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(), tumorStageDeriver
        ).curateTumorDetails(PATIENT_ID, CANNOT_CURATE, CANNOT_CURATE)

        assertThat(missing.name).isEqualTo("")
        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.PRIMARY_TUMOR,
                "$CANNOT_CURATE | $CANNOT_CURATE",
                "Could not find primary tumor config for input '$CANNOT_CURATE | $CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.primaryTumorEvaluatedInputs).isEqualTo(setOf("$CANNOT_CURATE | $CANNOT_CURATE"))
    }

    @Test
    fun `Should not override lesion locations for unknown biopsies and lesions`() {
        val feedTumorDetails = FeedTumorDetail(
            biopsyLocation = BIOPSY_LOCATION_INPUT,
            lesions = listOf(FeedLesion(CANNOT_CURATE))
        )
        val (tumorDetails, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(), tumorStageDeriver
        ).extract(PATIENT_ID, feedTumorDetails)
        assertThat(tumorDetails).isEqualTo(TumorDetails(otherLesions = emptyList(), otherSuspectedLesions = emptyList()))
        assertThat(evaluation.warnings).containsExactlyInAnyOrder(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LESION_LOCATION,
                BIOPSY_LOCATION_INPUT,
                "Could not find lesion location config for input '$BIOPSY_LOCATION_INPUT'"
            ),
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LESION_LOCATION,
                CANNOT_CURATE,
                "Could not find lesion location config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.lesionLocationEvaluatedInputs).containsExactlyInAnyOrder(
            BIOPSY_LOCATION_INPUT.lowercase(),
            CANNOT_CURATE.lowercase()
        )
    }

    @Test
    fun `Should override has liver lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasLiverLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.LIVER)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasLiverLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected liver lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasSuspectedLiverLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.LIVER)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasSuspectedLiverLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER).copy(suspected = true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has liver lesions when listed as biopsy`() {
        val expected = TumorDetails(biopsyLocation = curatedLocationLesionInput(LesionLocationCategory.LIVER), hasLiverLesions = true)
        val feedTumorDetails = FeedTumorDetail(biopsyLocation = locationLesionInput(LesionLocationCategory.LIVER).location)

        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected liver lesions when listed as biopsy`() {
        val tumor = baseTumor.copy(hasLiverLesions = null)
        val feedTumorDetails = FeedTumorDetail(biopsyLocation = locationLesionInput(LesionLocationCategory.LIVER).location)
        val expected =
            tumor.copy(biopsyLocation = curatedLocationLesionInput(LesionLocationCategory.LIVER), hasSuspectedLiverLesions = true)

        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER, true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has cns lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasCnsLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.CNS)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasCnsLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.CNS)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected cns lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasCnsLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.CNS)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasSuspectedCnsLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.CNS, true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has brain lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasBrainLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.BRAIN)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasBrainLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.BRAIN)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected brain lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasBrainLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.BRAIN)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasSuspectedBrainLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.BRAIN, true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has lymph node lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasLymphNodeLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.LYMPH_NODE)))
        val expected = tumor.copy(
            otherLesions = listOf(curatedLocationLesionInput(LesionLocationCategory.LYMPH_NODE)),
            otherSuspectedLesions = emptyList(),
            hasLymphNodeLesions = true
        )
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LYMPH_NODE)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected lymph node lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasLymphNodeLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.LYMPH_NODE)))
        val expected = tumor.copy(
            otherLesions = emptyList(),
            otherSuspectedLesions = listOf(curatedLocationLesionInput(LesionLocationCategory.LYMPH_NODE)),
            hasSuspectedLymphNodeLesions = true
        )
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LYMPH_NODE, true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has lymph node lesions and suspected lymph node lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasLymphNodeLesions = null)
        val feedTumorDetails = FeedTumorDetail(
            lesions = listOf(
                locationLesionInput(LesionLocationCategory.LYMPH_NODE, " clavicle"),
                locationLesionInput(LesionLocationCategory.LYMPH_NODE, " neck")
            )
        )
        val expected = tumor.copy(
            otherLesions = listOf(
                curatedLocationLesionInput(LesionLocationCategory.LYMPH_NODE, " neck")
            ),
            otherSuspectedLesions = listOf(
                curatedLocationLesionInput(LesionLocationCategory.LYMPH_NODE, " clavicle")
            ),
            hasLymphNodeLesions = true,
            hasSuspectedLymphNodeLesions = true
        )

        val lesionLocationCuration = TestCurationFactory.curationDatabase(
            lesionLocationConfig(LesionLocationCategory.LYMPH_NODE, true, " clavicle"),
            lesionLocationConfig(LesionLocationCategory.LYMPH_NODE, null, " neck")
        )
        assertTumorExtraction(
            TumorDetailsExtractor(
                lesionLocationCuration,
                TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has bone lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasBoneLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.BONE)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasBoneLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.BONE)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected bone lesions when listed in other lesions`() {
        val tumor = baseTumor.copy(hasBoneLesions = null)
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.BONE)))
        val expected = tumor.copy(otherLesions = emptyList(), otherSuspectedLesions = emptyList(), hasSuspectedBoneLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.BONE, true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should override has suspected liver lesions when suspected liver lesions are curated but then not override known lesions`() {
        val tumor = baseTumor.copy(hasLiverLesions = null, hasSuspectedLiverLesions = null)
        val feedTumorDetails = FeedTumorDetail(
            hasLiverLesions = true,
            lesions = listOf(locationLesionInput(LesionLocationCategory.LIVER))
        )
        val expected = tumor.copy(
            otherLesions = emptyList(),
            otherSuspectedLesions = emptyList(),
            hasLiverLesions = true,
            hasSuspectedLiverLesions = true
        )
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER).copy(suspected = true)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), feedTumorDetails, expected
        )
    }

    @Test
    fun `Should curate other lymph node lesions`() {
        val extractor = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(lesionLocationConfig(LesionLocationCategory.LYMPH_NODE)),
            TestCurationFactory.curationDatabase(),
            tumorStageDeriver
        )
        val lesions = listOf(
            locationLesionInput(category = LesionLocationCategory.LYMPH_NODE),
            FeedLesion(CANNOT_CURATE)
        )
        val (curatedLesions, evaluation) = extractor.curateOtherLesions(PATIENT_ID, lesions)
        assertThat(curatedLesions).isNotNull
        assertThat(curatedLesions).hasSize(1)
        assertThat(evaluation.warnings).containsExactlyInAnyOrderElementsOf(
            listOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.LESION_LOCATION,
                    CANNOT_CURATE,
                    "Could not find lesion location config for input '$CANNOT_CURATE'"
                )
            )
        )
        assertThat(evaluation.lesionLocationEvaluatedInputs).isEqualTo(lesions.map { it.location.lowercase() }.toSet())
    }

    @Test
    fun `Should curate other lesions without category`() {
        val extractor = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(
                lesionLocationConfig(LesionLocationCategory.LYMPH_NODE, true, " clavicle"),
                lesionLocationConfig(LesionLocationCategory.LYMPH_NODE, null, " neck"),
                lesionLocationConfig(LesionLocationCategory.LYMPH_NODE, null, " abdomen").copy(category = null)
            ),
            TestCurationFactory.curationDatabase(),
            tumorStageDeriver
        )
        val lesions = listOf(
            locationLesionInput(category = LesionLocationCategory.LYMPH_NODE, " clavicle"),
            locationLesionInput(category = LesionLocationCategory.LYMPH_NODE, " neck"),
            locationLesionInput(category = LesionLocationCategory.LYMPH_NODE, " abdomen"),
            FeedLesion(CANNOT_CURATE)
        )
        val (curatedLesions, evaluation) = extractor.curateOtherLesions(PATIENT_ID, lesions)
        assertThat(curatedLesions).isNotNull
        assertThat(curatedLesions).hasSize(3)
        assertThat(curatedLesions.filter { it.suspected == true }.size).isEqualTo(1)
        assertThat(evaluation.warnings).containsExactlyInAnyOrderElementsOf(
            listOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.LESION_LOCATION,
                    CANNOT_CURATE,
                    "Could not find lesion location config for input '$CANNOT_CURATE'"
                )
            )
        )
        assertThat(evaluation.lesionLocationEvaluatedInputs).isEqualTo(lesions.map { it.location.lowercase() }.toSet())
    }

    @Test
    fun `Should set brain and CNS lesion variables to false in case of primary brain tumor`() {
        val tumorDetailsExtractor =
            spyk(TumorDetailsExtractor(TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(), tumorStageDeriver))
        every {
            tumorDetailsExtractor.curateTumorDetails(
                inputTumorLocation = any(),
                inputTumorType = any(),
                patientId = any()
            )
        } returns (baseTumor.copy(name = "Brain") to CurationExtractionEvaluation())
        val expected = baseTumor.copy(
            name = "Brain",
            hasBrainLesions = false,
            hasActiveBrainLesions = false,
            hasSuspectedBrainLesions = false,
            hasCnsLesions = false,
            hasActiveCnsLesions = false,
            hasSuspectedCnsLesions = false,
        )
        assertTumorExtraction(tumorDetailsExtractor, FeedTumorDetail(), expected)
    }

    @Test
    fun `Should call deriver to derive stages`() {
        val tumorDetails = slot<TumorDetails>()
        val feedTumorDetails = FeedTumorDetail(lesions = listOf(locationLesionInput(LesionLocationCategory.BRAIN)))
        TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(
                lesionLocationConfig(LesionLocationCategory.BONE)
            ), TestCurationFactory.curationDatabase(
                PrimaryTumorConfig(
                    input = "$TUMOR_LOCATION_INPUT |",
                    ignore = false,
                    name = "",
                    doids = emptySet()
                )
            ),
            tumorStageDeriver
        ).extract(PATIENT_ID, feedTumorDetails)
        verify { tumorStageDeriver.derive(capture(tumorDetails)) }
        assertThat(tumorDetails).isNotNull
    }

    private fun lesionLocationConfig(category: LesionLocationCategory, suspected: Boolean? = null, extra: String? = "") =
        LesionLocationConfig(
            input = locationLesionInput(category, extra).location,
            ignore = false,
            location = curatedLocationLesionInput(category, extra),
            category = category,
            suspected = suspected
        )

    private fun assertTumorExtraction(extractor: TumorDetailsExtractor, feedTumorDetails: FeedTumorDetail, expected: TumorDetails) {
        val (tumorDetails, evaluation) = extractor.extract(PATIENT_ID, feedTumorDetails)
        assertThat(tumorDetails).isEqualTo(expected)
        assertThat(evaluation.warnings).isEmpty()
    }

    private fun locationLesionInput(category: LesionLocationCategory, extra: String? = ""): FeedLesion {
        return FeedLesion("${category.name.lowercase()} lesion input" + extra)
    }

    private fun curatedLocationLesionInput(category: LesionLocationCategory, extra: String? = ""): String {
        return "Curated ${category.name.lowercase()}" + extra
    }
}