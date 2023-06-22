package com.hartwig.actin.clinical.curation

import com.google.common.collect.Sets
import com.google.common.io.Resources
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.apache.logging.log4j.util.Strings
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CurationDatabaseReaderTest {
    private val reader = CurationDatabaseReader(TestCurationFactory.createMinimalTestCurationDatabaseValidator())
    private var database: CurationDatabase? = null

    @Before
    @Throws(IOException::class)
    fun createDatabase() {
        database = reader.read(CURATION_DIRECTORY)
    }

    @Test
    fun shouldReadPrimaryTumorConfigs() {
        val configs = database!!.primaryTumorConfigs
        assertEquals(1, configs.size.toLong())
        val config = configs[0]
        assertEquals("Unknown | Carcinoma", config.input)
        assertEquals("Unknown", config.primaryTumorLocation)
        assertEquals("CUP", config.primaryTumorSubLocation)
        assertEquals("Carcinoma", config.primaryTumorType)
        assertEquals(Strings.EMPTY, config.primaryTumorSubType)
        assertEquals(Strings.EMPTY, config.primaryTumorExtraDetails)
        assertEquals(1, config.doids.size.toLong())
        assertTrue(config.doids.contains("299"))
    }

    @Test
    fun shouldReadOncologicalHistoryConfigs() {
        val configs = database!!.oncologicalHistoryConfigs
        assertEquals(1, configs.size.toLong())
        val config = find(configs, "Capecitabine/Oxaliplatin 2020-2021")
        assertFalse(config.ignore)
        val curated = config.curated
        assertNotNull(curated)
        assertEquals("Capecitabine+Oxaliplatin", curated!!.name())
        assertIntegerEquals(2020, curated.startYear())
        assertNull(curated.startMonth())
        assertIntegerEquals(2021, curated.stopYear())
        assertNull(curated.stopMonth())
        assertIntegerEquals(6, curated.cycles())
        assertEquals("PR", curated.bestResponse())
        assertEquals("toxicity", curated.stopReason())
        assertEquals(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY), curated.categories())
        assertTrue(curated.isSystemic)
        assertEquals("antimetabolite,platinum", curated.chemoType())
        assertNull(curated.immunoType())
        assertNull(curated.targetedType())
        assertNull(curated.hormoneType())
        assertNull(curated.radioType())
        assertNull(curated.transplantType())
        assertNull(curated.supportiveType())
        assertNull(curated.trialAcronym())
        assertNull(curated.ablationType())
    }

    @Test
    fun shouldReadTreatmentHistoryConfigs() {
        val configs = database!!.treatmentHistoryEntryConfigs
        assertEquals(1, configs.size.toLong())
        val config = find(configs, "Capecitabine/Oxaliplatin 2020-2021")
        assertFalse(config.ignore)
        val curated = config.curated
        assertNotNull(curated)
        assertEquals(1, curated!!.treatments().size)
        val treatment = curated.treatments().iterator().next() as Therapy
        assertEquals("Capecitabine+Oxaliplatin", treatment.name())
        assertIntegerEquals(2020, curated.startYear())
        assertNull(curated.startMonth())
        assertIntegerEquals(2021, curated.therapyHistoryDetails()?.stopYear())
        assertNull(curated.therapyHistoryDetails()?.stopMonth())
        assertIntegerEquals(6, curated.therapyHistoryDetails()?.cycles())
        assertEquals(TreatmentResponse.PARTIAL_RESPONSE, curated.therapyHistoryDetails()?.bestResponse())
        assertEquals(StopReason.TOXICITY, curated.therapyHistoryDetails()?.stopReason())
        assertEquals(setOf(TreatmentCategory.CHEMOTHERAPY), treatment.categories())
        assertTrue(treatment.isSystemic)
        assertEquals(setOf(DrugClass.ANTIMETABOLITE, DrugClass.PLATINUM_COMPOUND), treatment.drugs().flatMap(Drug::drugClasses).toSet())
        assertEquals(setOf("Capecitabine", "Oxaliplatin"), treatment.drugs().map(Drug::name).toSet())
        assertNull(curated.trialAcronym())
    }

    @Test
    fun shouldReadSecondPrimaryConfigs() {
        val configs = database!!.secondPrimaryConfigs
        assertEquals(1, configs.size.toLong())
        val config = find(configs, "basaalcelcarcinoom (2014) | 2014")
        assertFalse(config.ignore)
        val curated = config.curated
        assertNotNull(curated)
        assertEquals(Strings.EMPTY, curated!!.tumorLocation())
        assertEquals(Strings.EMPTY, curated.tumorSubLocation())
        assertEquals("Carcinoma", curated.tumorType())
        assertEquals("Basal cell carcinoma", curated.tumorSubType())
        assertEquals(Sets.newHashSet("2513"), curated.doids())
        assertIntegerEquals(2014, curated.diagnosedYear())
        assertIntegerEquals(1, curated.diagnosedMonth())
        assertEquals("None", curated.treatmentHistory())
        assertIntegerEquals(2014, curated.lastTreatmentYear())
        assertIntegerEquals(2, curated.lastTreatmentMonth())
        assertFalse(curated.isActive)
    }

    @Test
    fun shouldReadLesionLocationConfigs() {
        val configs = database!!.lesionLocationConfigs
        assertEquals(1, configs.size.toLong())
        val config = configs[0]
        assertEquals("Lever", config.input)
        assertEquals("Liver", config.location)
    }

    @Test
    fun shouldReadNonOncologicalHistoryConfigs() {
        val configs = database!!.nonOncologicalHistoryConfigs
        assertEquals(4, configs.size.toLong())
        val config1 = find(configs, "Levercirrose/ sarcoidose")
        assertFalse(config1.ignore)
        assertTrue(config1.priorOtherCondition.isPresent)
        assertFalse(config1.lvef.isPresent)
        val curated1 = config1.priorOtherCondition.get()
        assertEquals("Liver cirrhosis and sarcoidosis", curated1.name())
        assertIntegerEquals(2019, curated1.year())
        assertIntegerEquals(7, curated1.month())
        assertEquals("Liver disease", curated1.category())
        assertEquals(2, curated1.doids().size.toLong())
        assertTrue(curated1.doids().contains("5082"))
        assertTrue(curated1.doids().contains("11335"))
        assertFalse(curated1.isContraindicationForTherapy)
        val config2 = find(configs, "NA")
        assertTrue(config2.ignore)
        assertFalse(config2.lvef.isPresent)
        assertFalse(config2.priorOtherCondition.isPresent)
        val config3 = find(configs, "LVEF 0.17")
        assertFalse(config3.ignore)
        assertTrue(config3.lvef.isPresent)
        assertFalse(config3.priorOtherCondition.isPresent)
        assertEquals(0.17, config3.lvef.get(), EPSILON)
        val config4 = find(configs, "No contraindication")
        assertTrue(config4.priorOtherCondition.isPresent)
        val curated4 = config4.priorOtherCondition.get()
        assertTrue(curated4.isContraindicationForTherapy)
    }

    @Test
    fun shouldReadECGConfigs() {
        val configs = database!!.ecgConfigs
        assertEquals(4, configs.size.toLong())
        val sinus = find(configs, "Sinus Tachycardia")
        assertEquals("Sinus tachycardia", sinus.interpretation)
        assertFalse(sinus.ignore)
        assertFalse(sinus.isQTCF)
        assertNull(sinus.qtcfValue)
        assertNull(sinus.qtcfUnit)
        assertFalse(sinus.isJTC)
        assertNull(sinus.jtcValue)
        assertNull(sinus.jtcUnit)
        val qtcf = find(configs, "qtcf")
        assertTrue(qtcf.isQTCF)
        assertFalse(qtcf.ignore)
        assertIntegerEquals(470, qtcf.qtcfValue)
        assertEquals("ms", qtcf.qtcfUnit)
        val jtc = find(configs, "jtc")
        assertTrue(jtc.isJTC)
        assertFalse(jtc.ignore)
        assertIntegerEquals(570, jtc.jtcValue)
        assertEquals("ms", jtc.jtcUnit)
        val weird = find(configs, "weird")
        assertTrue(weird.ignore)
    }

    @Test
    fun shouldReadInfectionConfigs() {
        val configs = database!!.infectionConfigs
        assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "YES lung abces")
        assertEquals("Lung abscess", config1.interpretation)
        val config2 = find(configs, "NA")
        assertEquals("No", config2.interpretation)
    }

    @Test
    fun shouldReadComplicationConfigs() {
        val configs = database!!.complicationConfigs
        assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "something")
        assertFalse(config1.ignore)
        assertFalse(config1.impliesUnknownComplicationState)
        val curated1 = config1.curated
        assertNotNull(curated1)
        assertEquals("curated something", curated1!!.name())
        assertEquals(2, curated1.categories().size.toLong())
        assertIntegerEquals(2000, curated1.year())
        assertIntegerEquals(1, curated1.month())
        val config2 = find(configs, "unknown")
        assertFalse(config2.ignore)
        assertTrue(config2.impliesUnknownComplicationState)
        val curated2 = config2.curated
        assertNotNull(curated2)
        assertEquals(Strings.EMPTY, curated2!!.name())
        assertEquals(0, curated2.categories().size.toLong())
        assertNull(curated2.year())
        assertNull(curated2.month())
    }

    @Test
    fun shouldReadToxicityConfigs() {
        val configs = database!!.toxicityConfigs
        assertEquals(1, configs.size.toLong())
        val config = configs[0]
        assertEquals("Neuropathy GR3", config.input)
        assertEquals("Neuropathy", config.name)
        assertEquals(Sets.newHashSet("Neuro"), config.categories)
        assertIntegerEquals(3, config.grade)
    }

    @Test
    fun shouldReadMolecularTestConfigs() {
        val configs = database!!.molecularTestConfigs
        assertEquals(1, configs.size.toLong())
        val config = configs[0]
        assertEquals("IHC ERBB2 3+", config.input)
        assertEquals(
            ImmutablePriorMolecularTest.builder()
                .test("IHC")
                .item("ERBB2")
                .measure(null)
                .scoreText(null)
                .scoreValuePrefix(null)
                .scoreValue(3.0)
                .scoreValueUnit("+")
                .impliesPotentialIndeterminateStatus(false)
                .build(), config.curated
        )
    }

    @Test
    fun shouldReadMedicationNameConfigs() {
        val configs = database!!.medicationNameConfigs
        assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "A en B")
        assertEquals("A and B", config1.name)
        assertFalse(config1.ignore)
        val config2 = find(configs, "No medication")
        assertTrue(config2.ignore)
    }

    @Test
    fun shouldReadMedicationDosageConfigs() {
        val configs = database!!.medicationDosageConfigs
        assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "once per day 50-60 mg")
        assertDoubleEquals(50.0, config1.dosageMin)
        assertDoubleEquals(60.0, config1.dosageMax)
        assertEquals("mg", config1.dosageUnit)
        assertDoubleEquals(1.0, config1.frequency)
        assertEquals("day", config1.frequencyUnit)
        assertEquals(false, config1.ifNeeded)
        val config2 = find(configs, "empty")
        assertNull(config2.dosageMin)
        assertNull(config2.dosageMax)
        assertNull(config2.dosageUnit)
        assertNull(config2.frequency)
        assertNull(config2.frequencyUnit)
        assertNull(config2.ifNeeded)
    }

    @Test
    fun shouldReadMedicationCategoryConfigs() {
        val configs = database!!.medicationCategoryConfigs
        assertEquals(2, configs.size.toLong())
        val paracetamol = find(configs, "Paracetamol")
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), paracetamol.categories)
        val formoterol = find(configs, "Formoterol and budesonide")
        assertEquals(Sets.newHashSet("Beta2 sympathomimetics", "Corticosteroids"), formoterol.categories)
    }

    @Test
    fun shouldReadAllergyConfigs() {
        val configs = database!!.intoleranceConfigs
        assertEquals(1, configs.size.toLong())
        val config = find(configs, "Clindamycine")
        assertEquals("Clindamycin", config.name)
        assertEquals(Sets.newHashSet("0060500"), config.doids)
    }

    @Test
    fun shouldReadAdministrationRouteTranslations() {
        val translations = database!!.administrationRouteTranslations
        assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        assertEquals("ORAAL", translation.administrationRoute)
        assertEquals("Oral", translation.translatedAdministrationRoute)
    }

    @Test
    fun shouldReadLaboratoryTranslations() {
        val translations = database!!.laboratoryTranslations
        assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        assertEquals("AC", translation.code)
        assertEquals("AC2", translation.translatedCode)
        assertEquals("ACTH", translation.name)
        assertEquals("Adrenocorticotropic hormone", translation.translatedName)
    }

    @Test
    fun shouldReadToxicityTranslations() {
        val translations = database!!.toxicityTranslations
        assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        assertEquals("Pijn", translation.toxicity)
        assertEquals("Pain", translation.translatedToxicity)
    }

    @Test
    fun shouldReadBloodTransfusionTranslations() {
        val translations = database!!.bloodTransfusionTranslations
        assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        assertEquals("Thrombocytenconcentraat", translation.product)
        assertEquals("Thrombocyte concentrate", translation.translatedProduct)
    }

    private fun assertIntegerEquals(expected: Int, actual: Int?) {
        assertNotNull(actual)
        assertEquals(expected.toLong(), (actual as Int).toLong())
    }

    private fun assertDoubleEquals(expected: Double, actual: Double?) {
        assertNotNull(actual)
        assertEquals(expected, actual!!, EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private val CURATION_DIRECTORY = Resources.getResource("curation").path
        private fun <T : CurationConfig> find(configs: List<T>, input: String): T {
            for (config in configs) {
                if (config.input == input) {
                    return config
                }
            }
            throw IllegalStateException("Could not find input '$input' in configs")
        }
    }
}