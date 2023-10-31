package com.hartwig.actin.clinical.curation

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.io.Resources
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patientId"

private const val CANNOT_CURATE = "cannot curate"

@Ignore
class CurationModelTest {

    private val model = TestCurationFactory.createProperTestCurationModel()

    @Test
    fun shouldBeAbleToCreateFromCurationDirectory() {
        assertNotNull(
            CurationModel.create(
                CURATION_DIRECTORY, TestDoidModelFactory.createMinimalTestDoidModel(),
                TreatmentDatabase(emptyMap(), emptyMap())
            )
        )
    }

    @Test
    fun shouldCurateTumorWithLocationAndType() {
        val curatedWithType: TumorDetails = model.curateTumorDetails(PATIENT_ID, "Unknown", "Carcinoma")
        assertEquals("Unknown", curatedWithType.primaryTumorLocation())
        assertEquals("Carcinoma", curatedWithType.primaryTumorType())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldCurateTumorWithLocationOnly() {
        val curatedWithoutType: TumorDetails = model.curateTumorDetails(PATIENT_ID, "Stomach", null)
        assertEquals("Stomach", curatedWithoutType.primaryTumorLocation())
        assertEquals(Strings.EMPTY, curatedWithoutType.primaryTumorType())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldCurateTumorWithTypeOnly() {
        val curatedWithoutLocation: TumorDetails = model.curateTumorDetails(PATIENT_ID, null, "Carcinoma")
        assertEquals(Strings.EMPTY, curatedWithoutLocation.primaryTumorLocation())
        assertEquals("Carcinoma", curatedWithoutLocation.primaryTumorType())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldNullTumorThatDoesNotExist() {
        val missing: TumorDetails = model.curateTumorDetails(PATIENT_ID, CANNOT_CURATE, CANNOT_CURATE)
        assertNull(missing.primaryTumorLocation())
        assertNull(missing.primaryTumorType())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.PRIMARY_TUMOR,
                "$CANNOT_CURATE | $CANNOT_CURATE",
                "Could not find primary tumor config for input '$CANNOT_CURATE | $CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldNotOverrideLesionLocationsForUnknownBiopsiesAndLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertEquals(base, model.overrideKnownLesionLocations(base, null, null))
        assertEquals(base, model.overrideKnownLesionLocations(base, "biopsy location", Lists.newArrayList("some other lesion")))
    }

    @Test
    fun shouldOverrideHasLiverLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasLiverLesions())

        val liverOther: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Lever"))
        assertEquals(true, liverOther.hasLiverLesions())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldOverrideHasLiverLesionsWhenListedAsBiopsy() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        val liverBiopsy: TumorDetails = model.overrideKnownLesionLocations(base, "lever", null)
        assertEquals(true, liverBiopsy.hasLiverLesions())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldOverrideHasCnsLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasCnsLesions())

        val cns: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("cns"))
        assertEquals(true, cns.hasCnsLesions())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldOverrideHasBrainLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasBrainLesions())

        val brain: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("brain"))
        assertEquals(true, brain.hasBrainLesions())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldOverrideHasLymphNodeLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasLymphNodeLesions())

        val lymphNode: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("lymph node"))
        assertEquals(true, lymphNode.hasLymphNodeLesions())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldOverrideHasBoneLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasBoneLesions())

        val bone: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Bone"))
        assertEquals(true, bone.hasBoneLesions())

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldCurateTreatmentHistory() {
        val treatmentHistory = listOf("Cis 2020 2021", "no systemic treatment", CANNOT_CURATE).flatMap {
            model.curateTreatmentHistoryEntry(
                PATIENT_ID, it
            )
        }
        assertEquals(2, treatmentHistory.size.toLong())
        assertTrue(treatmentHistory.any { 2020 == it.startYear() })
        assertTrue(treatmentHistory.any { 2021 == it.startYear() })

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.ONCOLOGICAL_HISTORY,
                CANNOT_CURATE,
                "Could not find treatment history or second primary config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCuratePriorSecondPrimaries() {
        val priorSecondPrimaries: List<PriorSecondPrimary> =
            model.curatePriorSecondPrimaries(PATIENT_ID, Lists.newArrayList("Breast cancer Jan-2018", CANNOT_CURATE))
        assertEquals(1, priorSecondPrimaries.size.toLong())
        assertEquals("Breast", priorSecondPrimaries[0].tumorLocation())
        assertTrue(model.curatePriorSecondPrimaries(PATIENT_ID, null).isEmpty())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.SECOND_PRIMARY,
                CANNOT_CURATE,
                "Could not find second primary or treatment history config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCuratePriorOtherConditions() {
        val priorOtherConditions: List<PriorOtherCondition> =
            model.curatePriorOtherConditions(PATIENT_ID, Lists.newArrayList("sickness", "not a condition", CANNOT_CURATE))
        assertEquals(1, priorOtherConditions.size.toLong())
        assertEquals("sick", priorOtherConditions[0].name())
        assertTrue(model.curatePriorOtherConditions(PATIENT_ID, null).isEmpty())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                CANNOT_CURATE,
                "Could not find non-oncological history config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCuratePriorMolecularTests() {
        val priorMolecularTests: List<PriorMolecularTest> =
            model.curatePriorMolecularTests(PATIENT_ID, "IHC", Lists.newArrayList("IHC ERBB2 3+", CANNOT_CURATE))
        assertEquals(1, priorMolecularTests.size.toLong())
        assertEquals("IHC", priorMolecularTests[0].test())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST,
                CANNOT_CURATE,
                "Could not find molecular test config for type 'IHC' with input: '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateComplications() {
        assertNull(model.curateComplications(PATIENT_ID, null))
        assertNull(model.curateComplications(PATIENT_ID, Lists.newArrayList()))

        val complications = model.curateComplications(PATIENT_ID, Lists.newArrayList("term", CANNOT_CURATE))
        assertNotNull(complications)
        assertEquals(1, complications!!.size.toLong())
        assertNotNull(findComplicationByName(complications, "Curated"))

        val ignore = model.curateComplications(PATIENT_ID, Lists.newArrayList("none"))
        assertNotNull(ignore)
        assertEquals(0, ignore!!.size.toLong())

        val unknown = model.curateComplications(PATIENT_ID, Lists.newArrayList("unknown"))
        assertNull(unknown)

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.COMPLICATION, CANNOT_CURATE, "Could not find complication config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateQuestionnaireToxicities() {
        val date = LocalDate.of(2018, 5, 21)
        val toxicities = model.curateQuestionnaireToxicities(PATIENT_ID, Lists.newArrayList("neuropathy gr3", CANNOT_CURATE), date)
        assertEquals(1, toxicities.size.toLong())
        val toxicity = toxicities[0]
        assertEquals("neuropathy", toxicity.name())
        assertEquals(Sets.newHashSet("neuro"), toxicity.categories())
        assertEquals(date, toxicity.evaluatedDate())
        assertEquals(ToxicitySource.QUESTIONNAIRE, toxicity.source())
        assertEquals(Integer.valueOf(3), toxicity.grade())

        assertTrue(model.curateQuestionnaireToxicities(PATIENT_ID, null, date).isEmpty())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.TOXICITY, CANNOT_CURATE, "Could not find toxicity config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateECGs() {
        assertAberrationDescription("Cleaned aberration", model.curateECG(PATIENT_ID, toECG("Weird aberration")))
        assertAberrationDescription("No curation needed", model.curateECG(PATIENT_ID, toECG("No curation needed")))
        assertAberrationDescription(null, model.curateECG(PATIENT_ID, toECG("Yes but unknown what aberration")))
        assertNull(model.curateECG(PATIENT_ID, toECG("No aberration")))
        assertNull(model.curateECG(PATIENT_ID, null))

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.ECG, "No curation needed", "Could not find ECG config for input 'No curation needed'"
            )
        )
    }

    @Test
    fun shouldCurateInfectionStatus() {
        assertInfectionDescription("Cleaned infection", model.curateInfectionStatus(PATIENT_ID, toInfection("Weird infection")))
        assertInfectionDescription("No curation needed", model.curateInfectionStatus(PATIENT_ID, toInfection("No curation needed")))
        assertInfectionDescription(null, model.curateInfectionStatus(PATIENT_ID, toInfection("No Infection")))
        assertNull(model.curateInfectionStatus(PATIENT_ID, null))

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
    fun shouldInterpretPeriodBetweenUnit() {
        assertNull(model.curatePeriodBetweenUnit(PATIENT_ID, null))
        assertNull(model.curatePeriodBetweenUnit(PATIENT_ID, Strings.EMPTY))
        assertEquals("months", model.curatePeriodBetweenUnit(PATIENT_ID, "mo"))
    }

    @Test
    fun shouldDetermineLVEF() {
        assertNull(model.determineLVEF(null))
        assertNull(model.determineLVEF(listOf("not an LVEF")))

        val lvef = model.determineLVEF(listOf("LVEF 0.17"))
        assertNotNull(lvef)
        assertEquals(0.17, lvef!!, EPSILON)

        assertThat(model.getWarnings(PATIENT_ID)).isEmpty()
    }

    @Test
    fun shouldCurateOtherLesions() {
        assertNull(model.curateOtherLesions(PATIENT_ID, null))

        val notALesionCuration = model.curateOtherLesions(PATIENT_ID, Lists.newArrayList("not a lesion"))
        assertNotNull(notALesionCuration)
        assertTrue(notALesionCuration!!.isEmpty())

        val noOtherLesionsCuration = model.curateOtherLesions(PATIENT_ID, Lists.newArrayList("No"))
        assertNotNull(noOtherLesionsCuration)
        assertTrue(noOtherLesionsCuration!!.isEmpty())

        val otherLesionsCuration =
            model.curateOtherLesions(PATIENT_ID, Lists.newArrayList("lymph node", "not a lesion", CANNOT_CURATE))
        assertNotNull(otherLesionsCuration)
        assertEquals(1, otherLesionsCuration!!.size.toLong())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LESION_LOCATION,
                CANNOT_CURATE,
                "Could not find lesion config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateBiopsyLocation() {
        assertEquals("Liver", model.curateBiopsyLocation(PATIENT_ID, "lever"))
        assertEquals(Strings.EMPTY, model.curateBiopsyLocation(PATIENT_ID, "Not a lesion"))
        assertNull(model.curateBiopsyLocation(PATIENT_ID, CANNOT_CURATE))
        assertNull(model.curateBiopsyLocation(PATIENT_ID, null))

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LESION_LOCATION,
                CANNOT_CURATE,
                "Could not find lesion config for biopsy location '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateMedicationDosage() {
        val medication = model.curateMedicationDosage(PATIENT_ID, "once per day 50-60 mg every month")
        assertNotNull(medication)
        assertDoubleEquals(50.0, medication!!.dosageMin())
        assertDoubleEquals(60.0, medication.dosageMax())
        assertEquals("mg", medication.dosageUnit())
        assertDoubleEquals(1.0, medication.frequency())
        assertEquals("day", medication.frequencyUnit())
        assertEquals(1.0, medication.periodBetweenValue())
        assertEquals("mo", medication.periodBetweenUnit())
        assertEquals(false, medication.ifNeeded())

        assertNull(model.curateMedicationDosage(PATIENT_ID, CANNOT_CURATE))

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MEDICATION_DOSAGE,
                CANNOT_CURATE,
                "Could not find medication dosage config for '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateMedicationName() {
        assertNull(model.curateMedicationName(PATIENT_ID, Strings.EMPTY))
        assertNull(model.curateMedicationName(PATIENT_ID, CANNOT_CURATE))
        assertNull(model.curateMedicationName(PATIENT_ID, "No medication"))
        assertEquals("A and B", model.curateMedicationName(PATIENT_ID, "A en B"))

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MEDICATION_NAME,
                CANNOT_CURATE,
                "Could not find medication name config for '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldCurateMedicationStatus() {
        assertNull(model.curateMedicationStatus(PATIENT_ID, Strings.EMPTY))
        assertEquals(MedicationStatus.ACTIVE, model.curateMedicationStatus(PATIENT_ID, "active"))
        assertEquals(MedicationStatus.ON_HOLD, model.curateMedicationStatus(PATIENT_ID, "on-hold"))
        assertEquals(MedicationStatus.CANCELLED, model.curateMedicationStatus(PATIENT_ID, "Kuur geannuleerd"))
        assertEquals(MedicationStatus.UNKNOWN, model.curateMedicationStatus(PATIENT_ID, "not a status"))
    }

    @Test
    fun shouldTranslateAdministrationRoute() {
        assertNull(model.translateAdministrationRoute(PATIENT_ID, null))
        assertNull(model.translateAdministrationRoute(PATIENT_ID, Strings.EMPTY))
        assertNull(model.translateAdministrationRoute(PATIENT_ID, "not a route"))
        assertNull(model.translateAdministrationRoute(PATIENT_ID, "ignore"))
        assertEquals("oral", model.translateAdministrationRoute(PATIENT_ID, "oraal"))
    }

    @Test
    fun shouldCurateIntolerances() {
        val proper: Intolerance = ImmutableIntolerance.builder()
            .name("Latex type 1")
            .category(Strings.EMPTY)
            .type(Strings.EMPTY)
            .clinicalStatus(Strings.EMPTY)
            .verificationStatus(Strings.EMPTY)
            .criticality(Strings.EMPTY)
            .build()
        val curatedProper = model.curateIntolerance(PATIENT_ID, proper)
        assertEquals("Latex (type 1)", curatedProper.name())
        assertTrue(curatedProper.doids().contains("0060532"))

        val passThrough: Intolerance = ImmutableIntolerance.builder().from(proper).name(CANNOT_CURATE).build()
        assertEquals(passThrough, model.curateIntolerance(PATIENT_ID, passThrough))

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.INTOLERANCE,
                "Cannot curate",
                "Could not find intolerance config for 'Cannot curate'"
            )
        )
    }

    @Test
    fun shouldTranslateLaboratoryValues() {
        val test: LabValue = ImmutableLabValue.builder()
            .date(LocalDate.of(2020, 1, 1))
            .code("CO")
            .name("naam")
            .comparator(Strings.EMPTY)
            .value(0.0)
            .unit(LabUnit.NONE)
            .isOutsideRef(false)
            .build()
        val translated: LabValue = model.translateLabValue(PATIENT_ID, test)
        assertEquals("CODE", translated.code())
        assertEquals("Name", translated.name())

        val notExisting: LabValue = ImmutableLabValue.builder().from(test).code(CANNOT_CURATE).name(CANNOT_CURATE).build()

        val notExistingTranslated: LabValue = model.translateLabValue(PATIENT_ID, notExisting)
        assertEquals(CANNOT_CURATE, notExistingTranslated.code())
        assertEquals(CANNOT_CURATE, notExistingTranslated.name())

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
    fun shouldTranslateToxicities() {
        val test: Toxicity =
            ImmutableToxicity.builder().name("Pijn").evaluatedDate(LocalDate.of(2020, 11, 11)).source(ToxicitySource.EHR).build()
        val translated = model.translateToxicity(PATIENT_ID, test)
        assertEquals("Pain", translated.name())

        val notExisting: Toxicity = ImmutableToxicity.builder().from(test).name(CANNOT_CURATE).build()

        val notExistingTranslated = model.translateToxicity(PATIENT_ID, notExisting)
        assertEquals(notExisting.name(), notExistingTranslated.name())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.TOXICITY_TRANSLATION,
                CANNOT_CURATE,
                "Could not find translation for toxicity with input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun shouldTranslateBloodTransfusions() {
        val test: BloodTransfusion = ImmutableBloodTransfusion.builder().date(LocalDate.of(2019, 9, 9)).product("Product").build()
        val translated: BloodTransfusion = model.translateBloodTransfusion(PATIENT_ID, test)
        assertEquals("Translated product", translated.product())

        val notExisting: BloodTransfusion = ImmutableBloodTransfusion.builder().from(test).product(CANNOT_CURATE).build()
        val notExistingTranslated: BloodTransfusion = model.translateBloodTransfusion(PATIENT_ID, notExisting)
        assertEquals(CANNOT_CURATE, notExistingTranslated.product())

        assertThat(model.getWarnings(PATIENT_ID)).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                CANNOT_CURATE,
                "Could not find blood transfusion translation for blood transfusion with product '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun canTranslateDosageUnit() {
        assertNull(model.translateDosageUnit(PATIENT_ID, null))
        assertNull(model.translateDosageUnit(PATIENT_ID, Strings.EMPTY))
        assertEquals("piece", model.translateDosageUnit(PATIENT_ID, "stuk"))
    }

    private fun assertDoubleEquals(expected: Double, actual: Double?) {
        assertNotNull(actual)
        assertEquals(expected, actual!!, EPSILON)
    }

    private fun assertAberrationDescription(expectedDescription: String?, curatedECG: ECG?) {
        assertNotNull(curatedECG)
        assertEquals(expectedDescription, curatedECG!!.aberrationDescription())
    }

    private fun assertInfectionDescription(expected: String?, infectionStatus: InfectionStatus?) {
        assertNotNull(infectionStatus)
        assertEquals(expected, infectionStatus!!.description())
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private val CURATION_DIRECTORY = Resources.getResource("curation").path

        private fun findComplicationByName(complications: List<Complication>, nameToFind: String): Complication {
            return complications.find { complication: Complication -> complication.name() == nameToFind }
                ?: throw IllegalStateException("Could not find complication with name '$nameToFind'")
        }

        private fun toECG(aberrationDescription: String): ECG {
            return ImmutableECG.builder().hasSigAberrationLatestECG(true).aberrationDescription(aberrationDescription).build()
        }

        private fun toInfection(description: String): InfectionStatus {
            return ImmutableInfectionStatus.builder().hasActiveInfection(true).description(description).build()
        }
    }
}