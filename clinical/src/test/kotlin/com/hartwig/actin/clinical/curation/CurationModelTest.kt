package com.hartwig.actin.clinical.curation

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.io.Resources
import com.hartwig.actin.clinical.datamodel.*
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.doid.TestDoidModelFactory
import junit.framework.TestCase.*
import org.apache.logging.log4j.util.Strings
import org.junit.Test
import java.time.LocalDate

class CurationModelTest {

    private val model = TestCurationFactory.createProperTestCurationModel()

    @Test
    fun canCreateFromCurationDirectory() {
        assertNotNull(CurationModel.create(CURATION_DIRECTORY, TestDoidModelFactory.createMinimalTestDoidModel()))
    }

    @Test
    fun canCurateTumorDetails() {
        val curated: TumorDetails = model.curateTumorDetails("Unknown", "Carcinoma")
        assertEquals("Unknown", curated.primaryTumorLocation())

        val missing: TumorDetails = model.curateTumorDetails("Does not", "Exist")
        assertNull(missing.primaryTumorLocation())

        model.evaluate()
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

        model.evaluate()
    }

    @Test
    fun shouldOverrideHasLiverLesionsWhenListedAsBiopsy() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        val liverBiopsy: TumorDetails = model.overrideKnownLesionLocations(base, "lever", null)
        assertEquals(true, liverBiopsy.hasLiverLesions())

        model.evaluate()
    }

    @Test
    fun shouldOverrideHasCnsLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasCnsLesions())

        val cns: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("cns"))
        assertEquals(true, cns.hasCnsLesions())

        model.evaluate()
    }

    @Test
    fun shouldOverrideHasBrainLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasBrainLesions())

        val brain: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("brain"))
        assertEquals(true, brain.hasBrainLesions())

        model.evaluate()
    }

    @Test
    fun shouldOverrideHasLymphNodeLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasLymphNodeLesions())

        val lymphNode: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("lymph node"))
        assertEquals(true, lymphNode.hasLymphNodeLesions())

        model.evaluate()
    }

    @Test
    fun shouldOverrideHasBoneLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        assertNull(base.hasBoneLesions())

        val bone: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Bone"))
        assertEquals(true, bone.hasBoneLesions())

        model.evaluate()
    }

    @Test
    fun canCuratePriorTreatments() {
        val priorTreatments: List<PriorTumorTreatment> =
            model.curatePriorTumorTreatments(Lists.newArrayList("Cis 2020 2021", "no systemic treatment", "cannot curate"))
        assertEquals(2, priorTreatments.size.toLong())
        assertTrue(priorTreatments.any { 2020 == it.startYear() })
        assertTrue(priorTreatments.any { 2021 == it.startYear() })
        assertTrue(model.curatePriorTumorTreatments(null).isEmpty())

        model.evaluate()
    }

    @Test
    fun canCuratePriorSecondPrimaries() {
        val priorSecondPrimaries: List<PriorSecondPrimary> =
            model.curatePriorSecondPrimaries(Lists.newArrayList("Breast cancer Jan-2018", "cannot curate"))
        assertEquals(1, priorSecondPrimaries.size.toLong())
        assertEquals("Breast", priorSecondPrimaries[0].tumorLocation())
        assertTrue(model.curatePriorSecondPrimaries(null).isEmpty())

        model.evaluate()
    }

    @Test
    fun canCuratePriorOtherConditions() {
        val priorOtherConditions: List<PriorOtherCondition> =
            model.curatePriorOtherConditions(Lists.newArrayList("sickness", "not a condition", "cannot curate"))
        assertEquals(1, priorOtherConditions.size.toLong())
        assertEquals("sick", priorOtherConditions[0].name())
        assertTrue(model.curatePriorOtherConditions(null).isEmpty())

        model.evaluate()
    }

    @Test
    fun canCuratePriorMolecularTests() {
        val priorMolecularTests: List<PriorMolecularTest> =
            model.curatePriorMolecularTests("IHC", Lists.newArrayList("IHC ERBB2 3+", "not a molecular test"))
        assertEquals(1, priorMolecularTests.size.toLong())
        assertEquals("IHC", priorMolecularTests[0].test())
        assertTrue(model.curatePriorTumorTreatments(null).isEmpty())

        model.evaluate()
    }

    @Test
    fun canCurateComplications() {
        assertNull(model.curateComplications(null))
        assertNull(model.curateComplications(Lists.newArrayList()))

        val complications = model.curateComplications(Lists.newArrayList("term", "no curation"))
        assertNotNull(complications)
        assertEquals(1, complications!!.size.toLong())
        assertNotNull(findComplicationByName(complications, "Curated"))

        val ignore = model.curateComplications(Lists.newArrayList("none"))
        assertNotNull(ignore)
        assertEquals(0, ignore!!.size.toLong())

        val unknown = model.curateComplications(Lists.newArrayList("unknown"))
        assertNull(unknown)

        model.evaluate()
    }

    @Test
    fun canCurateQuestionnaireToxicities() {
        val date = LocalDate.of(2018, 5, 21)
        val toxicities = model.curateQuestionnaireToxicities(Lists.newArrayList("neuropathy gr3", "cannot curate"), date)
        assertEquals(1, toxicities.size.toLong())
        val toxicity = toxicities[0]
        assertEquals("neuropathy", toxicity.name())
        assertEquals(Sets.newHashSet("neuro"), toxicity.categories())
        assertEquals(date, toxicity.evaluatedDate())
        assertEquals(ToxicitySource.QUESTIONNAIRE, toxicity.source())
        assertEquals(Integer.valueOf(3), toxicity.grade())

        assertTrue(model.curateQuestionnaireToxicities(null, date).isEmpty())

        model.evaluate()
    }

    @Test
    fun canCurateECGs() {
        assertAberrationDescription("Cleaned aberration", model.curateECG(toECG("Weird aberration")))
        assertAberrationDescription("No curation needed", model.curateECG(toECG("No curation needed")))
        assertAberrationDescription(null, model.curateECG(toECG("Yes but unknown what aberration")))
        assertNull(model.curateECG(toECG("No aberration")))
        assertNull(model.curateECG(null))

        model.evaluate()
    }

    @Test
    fun canCurateInfectionStatus() {
        assertInfectionDescription("Cleaned infection", model.curateInfectionStatus(toInfection("Weird infection")))
        assertInfectionDescription("No curation needed", model.curateInfectionStatus(toInfection("No curation needed")))
        assertInfectionDescription(null, model.curateInfectionStatus(toInfection("No Infection")))
        assertNull(model.curateInfectionStatus(null))

        model.evaluate()
    }

    @Test
    fun canDetermineLVEF() {
        assertNull(model.determineLVEF(null))
        assertNull(model.determineLVEF(listOf("not an LVEF")))

        val lvef = model.determineLVEF(listOf("LVEF 0.17"))
        assertNotNull(lvef)
        assertEquals(0.17, lvef!!, EPSILON)

        model.evaluate()
    }

    @Test
    fun canCurateOtherLesions() {
        assertNull(model.curateOtherLesions(null))

        val notALesionCuration = model.curateOtherLesions(Lists.newArrayList("not a lesion"))
        assertNotNull(notALesionCuration)
        assertTrue(notALesionCuration!!.isEmpty())

        val noOtherLesionsCuration = model.curateOtherLesions(Lists.newArrayList("No"))
        assertNotNull(noOtherLesionsCuration)
        assertTrue(noOtherLesionsCuration!!.isEmpty())

        val otherLesionsCuration = model.curateOtherLesions(Lists.newArrayList("lymph node", "not a lesion", "no curation available"))
        assertNotNull(otherLesionsCuration)
        assertEquals(1, otherLesionsCuration!!.size.toLong())

        model.evaluate()
    }

    @Test
    fun canCurateBiopsyLocation() {
        assertEquals("Liver", model.curateBiopsyLocation("lever"))
        assertEquals(Strings.EMPTY, model.curateBiopsyLocation("Not a lesion"))
        assertNull(model.curateBiopsyLocation("No curation configured"))
        assertNull(model.curateBiopsyLocation(null))

        model.evaluate()
    }

    @Test
    fun canCurateMedicationDosage() {
        val medication = model.curateMedicationDosage("50-60 mg per day")
        assertNotNull(medication)
        assertDoubleEquals(50.0, medication!!.dosageMin())
        assertDoubleEquals(60.0, medication.dosageMax())
        assertEquals("mg", medication.dosageUnit())
        assertDoubleEquals(1.0, medication.frequency())
        assertEquals("day", medication.frequencyUnit())
        assertEquals(false, medication.ifNeeded())

        assertNull(model.curateMedicationDosage("does not exist"))

        model.evaluate()
    }

    @Test
    fun canCurateMedicationName() {
        assertNull(model.curateMedicationName(Strings.EMPTY))
        assertNull(model.curateMedicationName("does not exist"))
        assertNull(model.curateMedicationName("No medication"))
        assertEquals("A and B", model.curateMedicationName("A en B"))

        model.evaluate()
    }

    @Test
    fun canCurateMedicationCodeATC() {
        assertEquals(Strings.EMPTY, model.curateMedicationCodeATC(Strings.EMPTY))
        assertEquals("N12", model.curateMedicationCodeATC("N12"))
        assertEquals(Strings.EMPTY, model.curateMedicationCodeATC("12N"))
    }

    @Test
    fun canCurateMedicationStatus() {
        assertNull(model.curateMedicationStatus(Strings.EMPTY))
        assertEquals(MedicationStatus.ACTIVE, model.curateMedicationStatus("active"))
        assertEquals(MedicationStatus.ON_HOLD, model.curateMedicationStatus("on-hold"))
        assertEquals(MedicationStatus.CANCELLED, model.curateMedicationStatus("Kuur geannuleerd"))
        assertEquals(MedicationStatus.UNKNOWN, model.curateMedicationStatus("not a status"))
    }

    @Test
    fun canAnnotateWithMedicationCategory() {
        val proper: Medication = TestMedicationFactory.builder().name("Paracetamol").build()
        val annotatedProper = model.annotateWithMedicationCategory(proper)
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), annotatedProper.categories())

        val empty: Medication = TestMedicationFactory.builder().name(Strings.EMPTY).build()
        val annotatedEmpty = model.annotateWithMedicationCategory(empty)
        assertEquals(empty, annotatedEmpty)

        model.evaluate()
    }

    @Test
    fun canTranslateAdministrationRoute() {
        assertNull(model.translateAdministrationRoute(null))
        assertNull(model.translateAdministrationRoute(Strings.EMPTY))
        assertNull(model.translateAdministrationRoute("not a route"))
        assertNull(model.translateAdministrationRoute("ignore"))
        assertEquals("oral", model.translateAdministrationRoute("oraal"))
    }

    @Test
    fun canCurateIntolerances() {
        val proper: Intolerance = ImmutableIntolerance.builder()
            .name("Latex type 1")
            .category(Strings.EMPTY)
            .type(Strings.EMPTY)
            .clinicalStatus(Strings.EMPTY)
            .verificationStatus(Strings.EMPTY)
            .criticality(Strings.EMPTY)
            .build()
        val curatedProper = model.curateIntolerance(proper)
        assertEquals("Latex (type 1)", curatedProper.name())
        assertTrue(curatedProper.doids().contains("0060532"))

        val passThrough: Intolerance = ImmutableIntolerance.builder().from(proper).name("don't curate me").build()
        assertEquals(passThrough, model.curateIntolerance(passThrough))

        val withSubCategory: Intolerance = ImmutableIntolerance.builder().from(proper).name("Paracetamol").category("Medication").build()
        assertTrue(model.curateIntolerance(withSubCategory).subcategories().contains("Acetanilide derivatives"))

        model.evaluate()
    }

    @Test
    fun canTranslateLaboratoryValues() {
        val test: LabValue = ImmutableLabValue.builder()
            .date(LocalDate.of(2020, 1, 1))
            .code("CO")
            .name("naam")
            .comparator(Strings.EMPTY)
            .value(0.0)
            .unit(LabUnit.NONE)
            .isOutsideRef(false)
            .build()
        val translated: LabValue = model.translateLabValue(test)
        assertEquals("CODE", translated.code())
        assertEquals("Name", translated.name())

        val notExisting: LabValue = ImmutableLabValue.builder().from(test).code("no").name("does not exist").build()

        val notExistingTranslated: LabValue = model.translateLabValue(notExisting)
        assertEquals("no", notExistingTranslated.code())
        assertEquals("does not exist", notExistingTranslated.name())

        model.evaluate()
    }

    @Test
    fun canTranslateToxicities() {
        val test: Toxicity =
            ImmutableToxicity.builder().name("Pijn").evaluatedDate(LocalDate.of(2020, 11, 11)).source(ToxicitySource.EHR).build()
        val translated = model.translateToxicity(test)
        assertEquals("Pain", translated.name())

        val notExisting: Toxicity = ImmutableToxicity.builder().from(test).name("something").build()

        val notExistingTranslated = model.translateToxicity(notExisting)
        assertEquals(notExisting.name(), notExistingTranslated.name())

        model.evaluate()
    }

    @Test
    fun canTranslateBloodTransfusions() {
        val test: BloodTransfusion = ImmutableBloodTransfusion.builder().date(LocalDate.of(2019, 9, 9)).product("Product").build()
        val translated: BloodTransfusion = model.translateBloodTransfusion(test)
        assertEquals("Translated product", translated.product())

        val notExisting: BloodTransfusion = ImmutableBloodTransfusion.builder().from(test).product("does not exist").build()
        val notExistingTranslated: BloodTransfusion = model.translateBloodTransfusion(notExisting)
        assertEquals("does not exist", notExistingTranslated.product())

        model.evaluate()
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