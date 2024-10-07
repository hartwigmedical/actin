package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory.emptyQuestionnaire
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val TUMOR_LOCATION_INPUT = "Tumor location input"

private const val TUMOR_TYPE_INPUT = "Tumor type input"

private const val CURATED_LOCATION = "Curated location"

private const val CURATED_TUMOR_TYPE = "Curated tumor type"

private const val BIOPSY_LOCATION_INPUT = "Biopsy location input"

class TumorDetailsExtractorTest {

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
                    primaryTumorType = "",
                    primaryTumorLocation = CURATED_LOCATION,
                    primaryTumorSubType = "",
                    primaryTumorSubLocation = "",
                    primaryTumorExtraDetails = "",
                    doids = emptySet()
                )
            ),
            tumorStageDeriver
        ).curateTumorDetails(PATIENT_ID, TUMOR_LOCATION_INPUT, null)
        assertThat(curatedWithoutType.primaryTumorLocation).isEqualTo(CURATED_LOCATION)
        assertThat(curatedWithoutType.primaryTumorType).isEmpty()

        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate tumor with type only`() {
        val (curatedWithoutLocation, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(
                PrimaryTumorConfig(
                    input = "| $TUMOR_TYPE_INPUT",
                    ignore = false,
                    primaryTumorType = CURATED_TUMOR_TYPE,
                    primaryTumorLocation = "",
                    primaryTumorSubType = "",
                    primaryTumorSubLocation = "",
                    primaryTumorExtraDetails = "",
                    doids = emptySet()
                )
            ),
            tumorStageDeriver
        ).curateTumorDetails(PATIENT_ID, null, TUMOR_TYPE_INPUT)
        assertThat(curatedWithoutLocation.primaryTumorLocation).isEmpty()
        assertThat(curatedWithoutLocation.primaryTumorType).isEqualTo(CURATED_TUMOR_TYPE)

        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should null tumor that does not exist`() {
        val (missing, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(), tumorStageDeriver
        ).curateTumorDetails(PATIENT_ID, CANNOT_CURATE, CANNOT_CURATE)
        assertThat(missing.primaryTumorLocation).isNull()
        assertThat(missing.primaryTumorType).isNull()

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
        val questionnaire = emptyQuestionnaire().copy(biopsyLocation = BIOPSY_LOCATION_INPUT, otherLesions = listOf(CANNOT_CURATE))
        val (tumorDetails, evaluation) = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(), TestCurationFactory.curationDatabase(), tumorStageDeriver
        ).extract(PATIENT_ID, questionnaire)
        assertThat(tumorDetails).isEqualTo(TumorDetails(otherLesions = emptyList()))
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
        assertThat(baseTumor.hasLiverLesions).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf(locationLesionInput(LesionLocationCategory.LIVER)))
        val expected = TumorDetails(otherLesions = emptyList(), hasLiverLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), questionnaire, expected
        )
    }

    @Test
    fun `Should override has liver lesions when listed as biopsy`() {
        assertThat(baseTumor.hasLiverLesions).isNull()
        val questionnaire = emptyQuestionnaire().copy(biopsyLocation = locationLesionInput(LesionLocationCategory.LIVER))
        val expected = TumorDetails(biopsyLocation = curatedLocationLesionInput(LesionLocationCategory.LIVER), hasLiverLesions = true)

        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LIVER)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), questionnaire, expected
        )
    }

    @Test
    fun `Should override has cns lesions when listed in other lesions`() {
        assertThat(baseTumor.hasCnsLesions).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf(locationLesionInput(LesionLocationCategory.CNS)))
        val expected = TumorDetails(otherLesions = emptyList(), hasCnsLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.CNS)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), questionnaire, expected
        )
    }

    @Test
    fun `Should override has brain lesions when listed in other lesions`() {
        assertThat(baseTumor.hasBrainLesions).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf(locationLesionInput(LesionLocationCategory.BRAIN)))
        val expected = TumorDetails(otherLesions = emptyList(), hasBrainLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.BRAIN)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), questionnaire, expected
        )
    }

    @Test
    fun `Should override has lymph node lesions when listed in other lesions`() {
        assertThat(baseTumor.hasLymphNodeLesions).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf(locationLesionInput(LesionLocationCategory.LYMPH_NODE)))
        val expected = TumorDetails(
            otherLesions = listOf(curatedLocationLesionInput(LesionLocationCategory.LYMPH_NODE)), hasLymphNodeLesions = true
        )
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.LYMPH_NODE)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), questionnaire, expected
        )
    }

    @Test
    fun `Should override has bone lesions when listed in other lesions`() {
        assertThat(baseTumor.hasBoneLesions).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf(locationLesionInput(LesionLocationCategory.BONE)))
        val expected = TumorDetails(otherLesions = emptyList(), hasBoneLesions = true)
        assertTumorExtraction(
            TumorDetailsExtractor(
                TestCurationFactory.curationDatabase(
                    lesionLocationConfig(LesionLocationCategory.BONE)
                ), TestCurationFactory.curationDatabase(),
                tumorStageDeriver
            ), questionnaire, expected
        )
    }


    @Test
    fun `Should curate other lesions`() {
        val extractor = TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(lesionLocationConfig(LesionLocationCategory.LYMPH_NODE)),
            TestCurationFactory.curationDatabase(),
            tumorStageDeriver
        )
        val lesions = listOf(locationLesionInput(category = LesionLocationCategory.LYMPH_NODE), CANNOT_CURATE)
        val (curatedLesions, evaluation) = extractor
            .curateOtherLesions(PATIENT_ID, lesions)
        assertThat(curatedLesions).isNotNull
        assertThat(curatedLesions!!).hasSize(1)
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
        assertThat(evaluation.lesionLocationEvaluatedInputs).isEqualTo(lesions.map(String::lowercase).toSet())
    }

    @Test
    fun `Should call deriver to derive stages`() {
        val tumorDetails = slot<TumorDetails>()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf(locationLesionInput(LesionLocationCategory.BRAIN)))

        TumorDetailsExtractor(
            TestCurationFactory.curationDatabase(
                lesionLocationConfig(LesionLocationCategory.BONE)
            ), TestCurationFactory.curationDatabase(
                PrimaryTumorConfig(
                    input = "$TUMOR_LOCATION_INPUT |",
                    ignore = false,
                    primaryTumorType = "",
                    primaryTumorLocation = CURATED_LOCATION,
                    primaryTumorSubType = "",
                    primaryTumorSubLocation = "",
                    primaryTumorExtraDetails = "",
                    doids = emptySet()
                )
            ),
            tumorStageDeriver
        ).extract(PATIENT_ID, questionnaire)
        verify { tumorStageDeriver.derive(capture(tumorDetails)) }
        assertThat(tumorDetails).isNotNull
    }

    private fun lesionLocationConfig(category: LesionLocationCategory) = LesionLocationConfig(
        input = locationLesionInput(category),
        ignore = false,
        location = curatedLocationLesionInput(category),
        category
    )

    private fun assertTumorExtraction(extractor: TumorDetailsExtractor, questionnaire: Questionnaire, expected: TumorDetails) {
        val (tumorDetails, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(tumorDetails).isEqualTo(expected)
        assertThat(evaluation.warnings).isEmpty()
    }

    private fun locationLesionInput(category: LesionLocationCategory): String {
        return "${category.name.lowercase()} lesion input"
    }

    private fun curatedLocationLesionInput(category: LesionLocationCategory): String {
        return "Curated ${category.name.lowercase()}"
    }

    private val baseTumor = TumorDetails()
}