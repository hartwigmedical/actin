package com.hartwig.actin.clinical.curation

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.io.Resources
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
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class CurationModelTest {
    private val model = TestCurationFactory.createProperTestCurationModel()

    @Test
    @Throws(IOException::class)
    fun canCreateFromCurationDirectory() {
        Assert.assertNotNull(CurationModel.create(CURATION_DIRECTORY, TestDoidModelFactory.createMinimalTestDoidModel()))
    }

    @Test
    fun canCurateTumorDetails() {
        val curated: TumorDetails = model.curateTumorDetails("Unknown", "Carcinoma")
        Assert.assertEquals("Unknown", curated.primaryTumorLocation())
        val missing: TumorDetails = model.curateTumorDetails("Does not", "Exist")
        Assert.assertNull(missing.primaryTumorLocation())
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
        Assert.assertNull(base.hasLiverLesions())
        val liverOther: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Lever"))
        Assert.assertEquals(true, liverOther.hasLiverLesions())
        model.evaluate()
    }

    @Test
    fun shouldOverrideHasLiverLesionsWhenListedAsBiopsy() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        val liverBiopsy: TumorDetails = model.overrideKnownLesionLocations(base, "lever", null)
        Assert.assertEquals(true, liverBiopsy.hasLiverLesions())
        model.evaluate()
    }

    @Test
    fun shouldOverrideHasCnsLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        Assert.assertNull(base.hasCnsLesions())
        val cns: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("cns"))
        Assert.assertEquals(true, cns.hasCnsLesions())
        model.evaluate()
    }

    @Test
    fun shouldOverrideHasBrainLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        Assert.assertNull(base.hasBrainLesions())
        val brain: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("brain"))
        Assert.assertEquals(true, brain.hasBrainLesions())
        model.evaluate()
    }

    @Test
    fun shouldOverrideHasLymphNodeLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        Assert.assertNull(base.hasLymphNodeLesions())
        val lymphNode: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("lymph node"))
        Assert.assertEquals(true, lymphNode.hasLymphNodeLesions())
        model.evaluate()
    }

    @Test
    fun shouldOverrideHasBoneLesionsWhenListedInOtherLesions() {
        val base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor()
        Assert.assertNull(base.hasBoneLesions())
        val bone: TumorDetails = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Bone"))
        Assert.assertEquals(true, bone.hasBoneLesions())
        model.evaluate()
    }

    @Test
    fun shouldCurateTreatmentHistory() {
        val treatmentHistory: List<TreatmentHistoryEntry> =
            model.curateTreatmentHistory(listOf("Cis 2020 2021", "no systemic treatment", "cannot curate"))
        Assert.assertEquals(2, treatmentHistory.size.toLong())
        Assert.assertTrue(treatmentHistory.stream().anyMatch { entry: TreatmentHistoryEntry -> Integer.valueOf(2020) == entry.startYear() })
        Assert.assertTrue(treatmentHistory.stream().anyMatch { entry: TreatmentHistoryEntry -> Integer.valueOf(2021) == entry.startYear() })
        Assert.assertTrue(model.curatePriorTumorTreatments(null).isEmpty())
        model.evaluate()
    }

    @Test
    fun canCuratePriorTreatments() {
        val priorTreatments: List<PriorTumorTreatment> =
            model.curatePriorTumorTreatments(Lists.newArrayList("Cis 2020 2021", "no systemic treatment", "cannot curate"))
        Assert.assertEquals(2, priorTreatments.size.toLong())
        Assert.assertTrue(priorTreatments.stream().anyMatch { entry: PriorTumorTreatment -> Integer.valueOf(2020) == entry.startYear() })
        Assert.assertTrue(priorTreatments.stream().anyMatch { entry: PriorTumorTreatment -> Integer.valueOf(2021) == entry.startYear() })
        Assert.assertTrue(model.curatePriorTumorTreatments(null).isEmpty())
        model.evaluate()
    }

    @Test
    fun canCuratePriorSecondPrimaries() {
        val priorSecondPrimaries: List<PriorSecondPrimary> =
            model.curatePriorSecondPrimaries(Lists.newArrayList("Breast cancer Jan-2018", "cannot curate"))
        Assert.assertEquals(1, priorSecondPrimaries.size.toLong())
        Assert.assertEquals("Breast", priorSecondPrimaries[0].tumorLocation())
        Assert.assertTrue(model.curatePriorSecondPrimaries(null).isEmpty())
        model.evaluate()
    }

    @Test
    fun canCuratePriorOtherConditions() {
        val priorOtherConditions: List<PriorOtherCondition> =
            model.curatePriorOtherConditions(Lists.newArrayList("sickness", "not a condition", "cannot curate"))
        Assert.assertEquals(1, priorOtherConditions.size.toLong())
        Assert.assertEquals("sick", priorOtherConditions[0].name())
        Assert.assertTrue(model.curatePriorOtherConditions(null).isEmpty())
        model.evaluate()
    }

    @Test
    fun canCuratePriorMolecularTests() {
        val priorMolecularTests: List<PriorMolecularTest> =
            model.curatePriorMolecularTests("IHC", Lists.newArrayList("IHC ERBB2 3+", "not a molecular test"))
        Assert.assertEquals(1, priorMolecularTests.size.toLong())
        Assert.assertEquals("IHC", priorMolecularTests[0].test())
        Assert.assertTrue(model.curatePriorTumorTreatments(null).isEmpty())
        model.evaluate()
    }

    @Test
    fun canCurateComplications() {
        Assert.assertNull(model.curateComplications(null))
        Assert.assertNull(model.curateComplications(Lists.newArrayList()))
        val complications = model.curateComplications(Lists.newArrayList("term", "no curation"))
        Assert.assertNotNull(complications)
        Assert.assertEquals(1, complications!!.size.toLong())
        Assert.assertNotNull(findComplicationByName(complications, "Curated"))
        val ignore = model.curateComplications(Lists.newArrayList("none"))
        Assert.assertNotNull(ignore)
        Assert.assertEquals(0, ignore!!.size.toLong())
        val unknown = model.curateComplications(Lists.newArrayList("unknown"))
        Assert.assertNull(unknown)
        model.evaluate()
    }

    @Test
    fun canCurateQuestionnaireToxicities() {
        val date = LocalDate.of(2018, 5, 21)
        val toxicities = model.curateQuestionnaireToxicities(Lists.newArrayList("neuropathy gr3", "cannot curate"), date)
        Assert.assertEquals(1, toxicities.size.toLong())
        val toxicity = toxicities[0]
        Assert.assertEquals("neuropathy", toxicity.name())
        Assert.assertEquals(Sets.newHashSet("neuro"), toxicity.categories())
        Assert.assertEquals(date, toxicity.evaluatedDate())
        Assert.assertEquals(ToxicitySource.QUESTIONNAIRE, toxicity.source())
        Assert.assertEquals(Integer.valueOf(3), toxicity.grade())
        Assert.assertTrue(model.curateQuestionnaireToxicities(null, date).isEmpty())
        model.evaluate()
    }

    @Test
    fun canCurateECGs() {
        assertAberrationDescription("Cleaned aberration", model.curateECG(toECG("Weird aberration")))
        assertAberrationDescription("No curation needed", model.curateECG(toECG("No curation needed")))
        assertAberrationDescription(null, model.curateECG(toECG("Yes but unknown what aberration")))
        Assert.assertNull(model.curateECG(toECG("No aberration")))
        Assert.assertNull(model.curateECG(null))
        model.evaluate()
    }

    @Test
    fun canCurateInfectionStatus() {
        assertInfectionDescription("Cleaned infection", model.curateInfectionStatus(toInfection("Weird infection")))
        assertInfectionDescription("No curation needed", model.curateInfectionStatus(toInfection("No curation needed")))
        assertInfectionDescription(null, model.curateInfectionStatus(toInfection("No Infection")))
        Assert.assertNull(model.curateInfectionStatus(null))
        model.evaluate()
    }

    @Test
    fun canDetermineLVEF() {
        Assert.assertNull(model.determineLVEF(null))
        Assert.assertNull(model.determineLVEF(Lists.newArrayList("not an LVEF")))
        val lvef = model.determineLVEF(Lists.newArrayList("LVEF 0.17"))
        Assert.assertNotNull(lvef)
        Assert.assertEquals(0.17, lvef!!, EPSILON)
        model.evaluate()
    }

    @Test
    fun canCurateOtherLesions() {
        Assert.assertNull(model.curateOtherLesions(null))
        val notALesionCuration = model.curateOtherLesions(Lists.newArrayList("not a lesion"))
        Assert.assertNotNull(notALesionCuration)
        Assert.assertTrue(notALesionCuration!!.isEmpty())
        val noOtherLesionsCuration = model.curateOtherLesions(Lists.newArrayList("No"))
        Assert.assertNotNull(noOtherLesionsCuration)
        Assert.assertTrue(noOtherLesionsCuration!!.isEmpty())
        val otherLesionsCuration = model.curateOtherLesions(Lists.newArrayList("lymph node", "not a lesion", "no curation available"))
        Assert.assertNotNull(otherLesionsCuration)
        Assert.assertEquals(1, otherLesionsCuration!!.size.toLong())
        model.evaluate()
    }

    @Test
    fun canCurateBiopsyLocation() {
        Assert.assertEquals("Liver", model.curateBiopsyLocation("lever"))
        Assert.assertEquals(Strings.EMPTY, model.curateBiopsyLocation("Not a lesion"))
        Assert.assertNull(model.curateBiopsyLocation("No curation configured"))
        Assert.assertNull(model.curateBiopsyLocation(null))
        model.evaluate()
    }

    @Test
    fun canCurateMedicationDosage() {
        val medication = model.curateMedicationDosage("50-60 mg per day")
        Assert.assertNotNull(medication)
        assertDoubleEquals(50.0, medication!!.dosageMin())
        assertDoubleEquals(60.0, medication.dosageMax())
        Assert.assertEquals("mg", medication.dosageUnit())
        assertDoubleEquals(1.0, medication.frequency())
        Assert.assertEquals("day", medication.frequencyUnit())
        Assert.assertEquals(false, medication.ifNeeded())
        Assert.assertNull(model.curateMedicationDosage("does not exist"))
        model.evaluate()
    }

    @Test
    fun canCurateMedicationName() {
        Assert.assertNull(model.curateMedicationName(Strings.EMPTY))
        Assert.assertNull(model.curateMedicationName("does not exist"))
        Assert.assertNull(model.curateMedicationName("No medication"))
        Assert.assertEquals("A and B", model.curateMedicationName("A en B"))
        model.evaluate()
    }

    @Test
    fun canCurateMedicationCodeATC() {
        Assert.assertEquals(Strings.EMPTY, model.curateMedicationCodeATC(Strings.EMPTY))
        Assert.assertEquals("N12", model.curateMedicationCodeATC("N12"))
        Assert.assertEquals(Strings.EMPTY, model.curateMedicationCodeATC("12N"))
    }

    @Test
    fun canCurateMedicationStatus() {
        val model = TestCurationFactory.createMinimalTestCurationModel()
        Assert.assertNull(model.curateMedicationStatus(Strings.EMPTY))
        assertEquals(MedicationStatus.ACTIVE, model.curateMedicationStatus("active"))
        assertEquals(MedicationStatus.ON_HOLD, model.curateMedicationStatus("on-hold"))
        assertEquals(MedicationStatus.CANCELLED, model.curateMedicationStatus("Kuur geannuleerd"))
        assertEquals(MedicationStatus.UNKNOWN, model.curateMedicationStatus("not a status"))
    }

    @Test
    fun canAnnotateWithMedicationCategory() {
        val proper: Medication = TestMedicationFactory.builder().name("Paracetamol").build()
        val annotatedProper = model.annotateWithMedicationCategory(proper)
        Assert.assertEquals(Sets.newHashSet("Acetanilide derivatives"), annotatedProper.categories())
        val empty: Medication = TestMedicationFactory.builder().name(Strings.EMPTY).build()
        val annotatedEmpty = model.annotateWithMedicationCategory(empty)
        Assert.assertEquals(empty, annotatedEmpty)
        model.evaluate()
    }

    @Test
    fun canTranslateAdministrationRoute() {
        Assert.assertNull(model.translateAdministrationRoute(null))
        Assert.assertNull(model.translateAdministrationRoute(Strings.EMPTY))
        Assert.assertNull(model.translateAdministrationRoute("not a route"))
        Assert.assertNull(model.translateAdministrationRoute("ignore"))
        Assert.assertEquals("oral", model.translateAdministrationRoute("oraal"))
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
        Assert.assertEquals("Latex (type 1)", curatedProper.name())
        Assert.assertTrue(curatedProper.doids().contains("0060532"))
        val passThrough: Intolerance = ImmutableIntolerance.builder().from(proper).name("don't curate me").build()
        Assert.assertEquals(passThrough, model.curateIntolerance(passThrough))
        val withSubCategory: Intolerance = ImmutableIntolerance.builder().from(proper).name("Paracetamol").category("Medication").build()
        Assert.assertTrue(model.curateIntolerance(withSubCategory).subcategories().contains("Acetanilide derivatives"))
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
        Assert.assertEquals("CODE", translated.code())
        Assert.assertEquals("Name", translated.name())
        val notExisting: LabValue = ImmutableLabValue.builder().from(test).code("no").name("does not exist").build()
        val notExistingTranslated: LabValue = model.translateLabValue(notExisting)
        Assert.assertEquals("no", notExistingTranslated.code())
        Assert.assertEquals("does not exist", notExistingTranslated.name())
        model.evaluate()
    }

    @Test
    fun canTranslateToxicities() {
        val test: Toxicity =
            ImmutableToxicity.builder().name("Pijn").evaluatedDate(LocalDate.of(2020, 11, 11)).source(ToxicitySource.EHR).build()
        val translated = model.translateToxicity(test)
        Assert.assertEquals("Pain", translated.name())
        val notExisting: Toxicity = ImmutableToxicity.builder().from(test).name("something").build()
        val notExistingTranslated = model.translateToxicity(notExisting)
        Assert.assertEquals(notExisting.name(), notExistingTranslated.name())
        model.evaluate()
    }

    @Test
    fun canTranslateBloodTransfusions() {
        val test: BloodTransfusion = ImmutableBloodTransfusion.builder().date(LocalDate.of(2019, 9, 9)).product("Product").build()
        val translated: BloodTransfusion = model.translateBloodTransfusion(test)
        Assert.assertEquals("Translated product", translated.product())
        val notExisting: BloodTransfusion = ImmutableBloodTransfusion.builder().from(test).product("does not exist").build()
        val notExistingTranslated: BloodTransfusion = model.translateBloodTransfusion(notExisting)
        Assert.assertEquals("does not exist", notExistingTranslated.product())
        model.evaluate()
    }

    private fun assertDoubleEquals(expected: Double, actual: Double?) {
        Assert.assertNotNull(actual)
        Assert.assertEquals(expected, actual!!, EPSILON)
    }

    private fun assertAberrationDescription(expectedDescription: String?, curatedECG: ECG?) {
        Assert.assertNotNull(curatedECG)
        Assert.assertEquals(expectedDescription, curatedECG!!.aberrationDescription())
    }

    private fun assertInfectionDescription(expected: String?, infectionStatus: InfectionStatus?) {
        Assert.assertNotNull(infectionStatus)
        Assert.assertEquals(expected, infectionStatus!!.description())
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private val CURATION_DIRECTORY = Resources.getResource("curation").path
        private fun findComplicationByName(complications: List<Complication>, nameToFind: String): Complication {
            return complications.stream()
                .filter { complication: Complication -> complication.name() == nameToFind }
                .findAny()
                .orElseThrow { IllegalStateException("Could not find complication with name '$nameToFind'") }
        }

        private fun toECG(aberrationDescription: String): ECG {
            return ImmutableECG.builder().hasSigAberrationLatestECG(true).aberrationDescription(aberrationDescription).build()
        }

        private fun toInfection(description: String): InfectionStatus {
            return ImmutableInfectionStatus.builder().hasActiveInfection(true).description(description).build()
        }
    }
}