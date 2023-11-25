package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory.emptyQuestionnaire
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class TumorDetailsExtractorTest {
    private val extractor = TumorDetailsExtractor(TestCurationFactory.createProperTestCurationDatabase())
    private val baseTumor = ImmutableTumorDetails.builder().build()

    @Test
    fun `Should curate tumor with location only`() {
        val (curatedWithoutType, evaluation) = extractor.curateTumorDetails(PATIENT_ID, "Stomach", null)
        assertThat(curatedWithoutType.primaryTumorLocation()).isEqualTo("Stomach")
        assertThat(curatedWithoutType.primaryTumorType()).isEmpty()

        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate tumor with type only`() {
        val (curatedWithoutLocation, evaluation) = extractor.curateTumorDetails(PATIENT_ID, null, "Carcinoma")
        assertThat(curatedWithoutLocation.primaryTumorLocation()).isEmpty()
        assertThat(curatedWithoutLocation.primaryTumorType()).isEqualTo("Carcinoma")

        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should null tumor that does not exist`() {
        val (missing, evaluation) = extractor.curateTumorDetails(PATIENT_ID, CANNOT_CURATE, CANNOT_CURATE)
        assertThat(missing.primaryTumorLocation()).isNull()
        assertThat(missing.primaryTumorType()).isNull()

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
        assertTumorExtraction(emptyQuestionnaire(), ImmutableTumorDetails.builder().build())

        val questionnaire = emptyQuestionnaire().copy(biopsyLocation = "biopsy location", otherLesions = listOf("some other lesion"))
        val (tumorDetails, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(tumorDetails).isEqualTo(ImmutableTumorDetails.builder().otherLesions(emptyList()).build())
        assertThat(evaluation.warnings).containsExactlyInAnyOrder(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LESION_LOCATION,
                "biopsy location",
                "Could not find lesion location config for input 'biopsy location'"
            ),
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LESION_LOCATION,
                "some other lesion",
                "Could not find lesion location config for input 'some other lesion'"
            )
        )
        assertThat(evaluation.lesionLocationEvaluatedInputs).containsExactlyInAnyOrder("biopsy location", "some other lesion")
    }

    @Test
    fun `Should override has liver lesions when listed in other lesions`() {
        assertThat(baseTumor.hasLiverLesions()).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf("Lever"))
        val expected = ImmutableTumorDetails.builder().otherLesions(emptyList()).hasLiverLesions(true).build()
        assertTumorExtraction(questionnaire, expected)
    }

    @Test
    fun `Should override has liver lesions when listed as biopsy`() {
        assertThat(baseTumor.hasLiverLesions()).isNull()
        val questionnaire = emptyQuestionnaire().copy(biopsyLocation = "lever")
        val expected = ImmutableTumorDetails.builder().biopsyLocation("Liver").hasLiverLesions(true).build()
        assertTumorExtraction(questionnaire, expected)
    }

    @Test
    fun `Should override has cns lesions when listed in other lesions`() {
        assertThat(baseTumor.hasCnsLesions()).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf("cns"))
        val expected = ImmutableTumorDetails.builder().otherLesions(emptyList()).hasCnsLesions(true).build()
        assertTumorExtraction(questionnaire, expected)
    }

    @Test
    fun `Should override has brain lesions when listed in other lesions`() {
        assertThat(baseTumor.hasBrainLesions()).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf("brain"))
        val expected = ImmutableTumorDetails.builder().otherLesions(emptyList()).hasBrainLesions(true).build()
        assertTumorExtraction(questionnaire, expected)
    }

    @Test
    fun `Should override has lymph node lesions when listed in other lesions`() {
        assertThat(baseTumor.hasLymphNodeLesions()).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf("lymph node"))
        val expected = ImmutableTumorDetails.builder().addOtherLesions("Lymph node").hasLymphNodeLesions(true).build()
        assertTumorExtraction(questionnaire, expected)
    }

    @Test
    fun `Should override has bone lesions when listed in other lesions`() {
        assertThat(baseTumor.hasBoneLesions()).isNull()
        val questionnaire = emptyQuestionnaire().copy(otherLesions = listOf("Bone"))
        val expected = ImmutableTumorDetails.builder().otherLesions(emptyList()).hasBoneLesions(true).build()
        assertTumorExtraction(questionnaire, expected)
    }

    private fun assertTumorExtraction(questionnaire: Questionnaire, expected: ImmutableTumorDetails) {
        val (tumorDetails, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(tumorDetails).isEqualTo(expected)
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate other lesions`() {
        assertThat(extractor.curateOtherLesions(PATIENT_ID, null).extracted).isNull()
        assertLesionCuration(listOf("not a lesion"), 0)
        assertLesionCuration(listOf("No"), 0)
        assertLesionCuration(
            listOf("lymph node", "not a lesion", CANNOT_CURATE), 1, listOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.LESION_LOCATION,
                    CANNOT_CURATE,
                    "Could not find lesion location config for input '$CANNOT_CURATE'"
                )
            )
        )
    }

    private fun assertLesionCuration(lesions: List<String>, numExpected: Int, expectedWarnings: List<CurationWarning> = emptyList()) {
        val (curatedLesions, evaluation) = extractor.curateOtherLesions(PATIENT_ID, lesions)
        assertThat(curatedLesions).isNotNull
        assertThat(curatedLesions!!).hasSize(numExpected)
        assertThat(evaluation.warnings).containsExactlyInAnyOrderElementsOf(expectedWarnings)
        assertThat(evaluation.lesionLocationEvaluatedInputs).isEqualTo(lesions.map(String::lowercase).toSet())
    }

    @Test
    fun `Should curate biopsy location`() {
        assertBiopsyLocationCuration("lever", "Liver")
        assertBiopsyLocationCuration("Not a lesion", "")
        assertBiopsyLocationCuration(
            CANNOT_CURATE, null, listOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.LESION_LOCATION,
                    CANNOT_CURATE,
                    "Could not find lesion location config for input '$CANNOT_CURATE'"
                )
            )
        )
        assertBiopsyLocationCuration(null, null)
    }

    private fun assertBiopsyLocationCuration(input: String?, expected: String?, expectedWarnings: List<CurationWarning> = emptyList()) {
        val questionnaire = emptyQuestionnaire().copy(biopsyLocation = input)
        val (tumorDetails, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(tumorDetails.biopsyLocation()).isEqualTo(expected)
        assertThat(evaluation.warnings).containsExactlyInAnyOrderElementsOf(expectedWarnings)
        assertThat(evaluation.lesionLocationEvaluatedInputs).isEqualTo(setOfNotNull(input?.lowercase()))
    }


    /*
    
    
        @Test
        fun `Should curate ECGs`() {
            assertAberrationDescription("Cleaned aberration", model.curateECG(PATIENT_ID, toECG("Weird aberration")))
            assertAberrationDescription("No curation needed", model.curateECG(PATIENT_ID, toECG("No curation needed")))
            assertAberrationDescription(null, model.curateECG(PATIENT_ID, toECG("Yes but unknown what aberration")))
            assertThat(model.curateECG(PATIENT_ID, toECG("No aberration"))).isNull()
            assertThat(model.curateECG(PATIENT_ID, null)).isNull()
    
            assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
                CurationWarning(
                    PATIENT_ID, CurationCategory.ECG, "No curation needed", "Could not find ECG config for input 'No curation needed'"
                )
            )
        }
    
        @Test
        fun `Should curate infection status`() {
            assertInfectionDescription("Cleaned infection", model.curateInfectionStatus(PATIENT_ID, toInfection("Weird infection")))
            assertInfectionDescription("No curation needed", model.curateInfectionStatus(PATIENT_ID, toInfection("No curation needed")))
            assertInfectionDescription(null, model.curateInfectionStatus(PATIENT_ID, toInfection("No Infection")))
            assertThat(model.curateInfectionStatus(PATIENT_ID, null)).isNull()
    
            assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.INFECTION,
                    "No curation needed",
                    "Could not find infection config for input 'No curation needed'"
                )
            )
        }
    
        @Test
        fun `Should determine LVEF`() {
            assertThat(model.determineLVEF(null)).isNull()
            assertThat(model.determineLVEF(listOf("not an LVEF"))).isNull()
    
            val lvef = model.determineLVEF(listOf("LVEF 0.17"))
            assertThat(lvef).isNotNull
            assertEquals(0.17, lvef!!, EPSILON)
    
            assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
        }
    
        @Test
        fun `Should curate intolerances`() {
            val proper: Intolerance = ImmutableIntolerance.builder()
                .name("Latex type 1")
                .category("")
                .type("")
                .clinicalStatus("")
                .verificationStatus("")
                .criticality("")
                .build()
            val curatedProper = model.curateIntolerance(PATIENT_ID, proper)
            assertThat(curatedProper.name()).isEqualTo("Latex (type 1)")
            assertThat(curatedProper.doids()).contains("0060532")
    
            val passThrough: Intolerance = ImmutableIntolerance.builder().from(proper).name(CANNOT_CURATE).build()
            assertThat(model.curateIntolerance(PATIENT_ID, passThrough)).isEqualTo(passThrough)
    
            assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.INTOLERANCE,
                    "Cannot curate",
                    "Could not find intolerance config for input 'Cannot curate'"
                )
            )
        }
    
        @Test
        fun `Should translate laboratory values`() {
            val test: LabValue = ImmutableLabValue.builder()
                .date(LocalDate.of(2020, 1, 1))
                .code("CO")
                .name("naam")
                .comparator("")
                .value(0.0)
                .unit(LabUnit.NONE)
                .isOutsideRef(false)
                .build()
            val translated: LabValue = model.translateLabValue(PATIENT_ID, test)
            assertThat(translated.code()).isEqualTo("CODE")
            assertThat(translated.name()).isEqualTo("Name")
    
            val notExisting: LabValue = ImmutableLabValue.builder().from(test).code(CANNOT_CURATE).name(CANNOT_CURATE).build()
    
            val notExistingTranslated: LabValue = model.translateLabValue(PATIENT_ID, notExisting)
            assertThat(notExistingTranslated.code()).isEqualTo(CANNOT_CURATE)
            assertThat(notExistingTranslated.name()).isEqualTo(CANNOT_CURATE)
    
            assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.LABORATORY_TRANSLATION,
                    CANNOT_CURATE,
                    "Could not find laboratory translation for lab value with code '$CANNOT_CURATE' and name '$CANNOT_CURATE'"
                )
            )
        }
    
        @Test
        fun `Should translate blood transfusions`() {
            val test: BloodTransfusion = ImmutableBloodTransfusion.builder().date(LocalDate.of(2019, 9, 9)).product("Product").build()
            val translated: BloodTransfusion = model.translateBloodTransfusion(PATIENT_ID, test)
            assertThat(translated.product()).isEqualTo("Translated product")
    
            val notExisting: BloodTransfusion = ImmutableBloodTransfusion.builder().from(test).product(CANNOT_CURATE).build()
            val notExistingTranslated: BloodTransfusion = model.translateBloodTransfusion(PATIENT_ID, notExisting)
            assertThat(notExistingTranslated.product()).isEqualTo(CANNOT_CURATE)
    
            assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                    CANNOT_CURATE,
                    "No translation found for blood transfusion with product: '$CANNOT_CURATE'"
                )
            )
        }
        
        private fun assertDoubleEquals(expected: Double, actual: Double?) {
            assertThat(actual).isNotNull
            assertEquals(expected, actual!!, EPSILON)
        }
    
        private fun assertAberrationDescription(expectedDescription: String?, curatedECG: ECG?) {
            assertThat(curatedECG).isNotNull
            assertThat(curatedECG!!.aberrationDescription()).isEqualTo(expectedDescription)
        }
    
        private fun assertInfectionDescription(expected: String?, infectionStatus: InfectionStatus?) {
            assertThat(infectionStatus).isNotNull
            assertThat(infectionStatus!!.description()).isEqualTo(expected)
        }
    
        companion object {
            private const val EPSILON = 1.0E-10
    
            private fun toECG(aberrationDescription: String): ECG {
                return ImmutableECG.builder().hasSigAberrationLatestECG(true).aberrationDescription(aberrationDescription).build()
            }
    
            private fun toInfection(description: String): InfectionStatus {
                return ImmutableInfectionStatus.builder().hasActiveInfection(true).description(description).build()
            }
        }
        
     */

}