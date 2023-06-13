package com.hartwig.actin.clinical.curation

import com.google.common.collect.Sets
import com.google.common.io.Resources
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.lang.Boolean
import java.util.Set
import kotlin.Double
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.String
import kotlin.Throws

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
        val configs = database!!.primaryTumorConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = configs[0]
        Assert.assertEquals("Unknown | Carcinoma", config!!.input())
        Assert.assertEquals("Unknown", config.primaryTumorLocation())
        Assert.assertEquals("CUP", config.primaryTumorSubLocation())
        Assert.assertEquals("Carcinoma", config.primaryTumorType())
        Assert.assertEquals(Strings.EMPTY, config.primaryTumorSubType())
        Assert.assertEquals(Strings.EMPTY, config.primaryTumorExtraDetails())
        Assert.assertEquals(1, config.doids().size.toLong())
        Assert.assertTrue(config.doids().contains("299"))
    }

    @Test
    fun shouldReadTreatmentHistoryEntryConfigs() {
        val configs = database!!.treatmentHistoryEntryConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = find(configs, "Capecitabine/Oxaliplatin 2020-2021")
        Assert.assertFalse(config!!.ignore())
        val curated = config.curated()
        Assert.assertNotNull(curated)
        val treatment = curated!!.treatments().iterator().next()
        Assert.assertEquals("Capecitabine+Oxaliplatin", treatment.name())
        assertIntegerEquals(2020, curated.startYear())
        Assert.assertNull(curated.startMonth())
        val therapyHistoryDetails = curated.therapyHistoryDetails()
        Assert.assertNotNull(therapyHistoryDetails)
        assertIntegerEquals(2021, therapyHistoryDetails!!.stopYear())
        Assert.assertNull(therapyHistoryDetails.stopMonth())
        assertIntegerEquals(6, therapyHistoryDetails.cycles())
        Assert.assertEquals(TreatmentResponse.PARTIAL_RESPONSE, therapyHistoryDetails.bestResponse())
        Assert.assertEquals(StopReason.TOXICITY, therapyHistoryDetails.stopReason())
        Assert.assertEquals(Set.of(TreatmentCategory.CHEMOTHERAPY), treatment.categories())
        Assert.assertTrue(treatment.isSystemic)
        Assert.assertNull(curated.trialAcronym())
    }

    @Test
    fun shouldReadOncologicalHistoryConfigs() {
        val configs = database!!.oncologicalHistoryConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = find(configs, "Capecitabine/Oxaliplatin 2020-2021")
        Assert.assertFalse(config!!.ignore())
        val curated = config.curated()
        Assert.assertNotNull(curated)
        Assert.assertEquals("Capecitabine+Oxaliplatin", curated!!.name())
        assertIntegerEquals(2020, curated.startYear())
        Assert.assertNull(curated.startMonth())
        assertIntegerEquals(2021, curated.stopYear())
        Assert.assertNull(curated.stopMonth())
        assertIntegerEquals(6, curated.cycles())
        Assert.assertEquals("PR", curated.bestResponse())
        Assert.assertEquals("toxicity", curated.stopReason())
        Assert.assertEquals(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY), curated.categories())
        Assert.assertTrue(curated.isSystemic)
        Assert.assertEquals("antimetabolite,platinum", curated.chemoType())
        Assert.assertNull(curated.immunoType())
        Assert.assertNull(curated.targetedType())
        Assert.assertNull(curated.hormoneType())
        Assert.assertNull(curated.radioType())
        Assert.assertNull(curated.transplantType())
        Assert.assertNull(curated.supportiveType())
        Assert.assertNull(curated.trialAcronym())
        Assert.assertNull(curated.ablationType())
    }

    @Test
    fun shouldReadSecondPrimaryConfigs() {
        val configs = database!!.secondPrimaryConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = find(configs, "basaalcelcarcinoom (2014) | 2014")
        Assert.assertFalse(config!!.ignore())
        val curated = config.curated()
        Assert.assertNotNull(curated)
        Assert.assertEquals(Strings.EMPTY, curated!!.tumorLocation())
        Assert.assertEquals(Strings.EMPTY, curated.tumorSubLocation())
        Assert.assertEquals("Carcinoma", curated.tumorType())
        Assert.assertEquals("Basal cell carcinoma", curated.tumorSubType())
        Assert.assertEquals(Sets.newHashSet("2513"), curated.doids())
        assertIntegerEquals(2014, curated.diagnosedYear())
        assertIntegerEquals(1, curated.diagnosedMonth())
        Assert.assertEquals("None", curated.treatmentHistory())
        assertIntegerEquals(2014, curated.lastTreatmentYear())
        assertIntegerEquals(2, curated.lastTreatmentMonth())
        Assert.assertFalse(curated.isActive)
    }

    @Test
    fun shouldReadLesionLocationConfigs() {
        val configs = database!!.lesionLocationConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = configs[0]
        Assert.assertEquals("Lever", config!!.input())
        Assert.assertEquals("Liver", config.location())
    }

    @Test
    fun shouldReadNonOncologicalHistoryConfigs() {
        val configs = database!!.nonOncologicalHistoryConfigs()
        Assert.assertEquals(4, configs.size.toLong())
        val config1 = find(configs, "Levercirrose/ sarcoidose")
        Assert.assertFalse(config1!!.ignore())
        Assert.assertTrue(config1.priorOtherCondition()!!.isPresent)
        Assert.assertFalse(config1.lvef()!!.isPresent)
        val curated1 = config1.priorOtherCondition()!!.get()
        Assert.assertEquals("Liver cirrhosis and sarcoidosis", curated1.name())
        assertIntegerEquals(2019, curated1.year())
        assertIntegerEquals(7, curated1.month())
        Assert.assertEquals("Liver disease", curated1.category())
        Assert.assertEquals(2, curated1.doids().size.toLong())
        Assert.assertTrue(curated1.doids().contains("5082"))
        Assert.assertTrue(curated1.doids().contains("11335"))
        Assert.assertFalse(curated1.isContraindicationForTherapy)
        val config2 = find(configs, "NA")
        Assert.assertTrue(config2!!.ignore())
        Assert.assertFalse(config2.lvef()!!.isPresent)
        Assert.assertFalse(config2.priorOtherCondition()!!.isPresent)
        val config3 = find(configs, "LVEF 0.17")
        Assert.assertFalse(config3!!.ignore())
        Assert.assertTrue(config3.lvef()!!.isPresent)
        Assert.assertFalse(config3.priorOtherCondition()!!.isPresent)
        Assert.assertEquals(0.17, config3.lvef()!!.get(), EPSILON)
        val config4 = find(configs, "No contraindication")
        Assert.assertTrue(config4!!.priorOtherCondition()!!.isPresent)
        val curated4 = config4.priorOtherCondition()!!.get()
        Assert.assertTrue(curated4.isContraindicationForTherapy)
    }

    @Test
    fun shouldReadECGConfigs() {
        val configs = database!!.ecgConfigs()
        Assert.assertEquals(4, configs.size.toLong())
        val sinus = find(configs, "Sinus Tachycardia")
        Assert.assertEquals("Sinus tachycardia", sinus!!.interpretation())
        Assert.assertFalse(sinus.ignore())
        Assert.assertFalse(sinus.isQTCF)
        Assert.assertNull(sinus.qtcfValue())
        Assert.assertNull(sinus.qtcfUnit())
        Assert.assertFalse(sinus.isJTC)
        Assert.assertNull(sinus.jtcValue())
        Assert.assertNull(sinus.jtcUnit())
        val qtcf = find(configs, "qtcf")
        Assert.assertTrue(qtcf!!.isQTCF)
        Assert.assertFalse(qtcf.ignore())
        assertIntegerEquals(470, qtcf.qtcfValue())
        Assert.assertEquals("ms", qtcf.qtcfUnit())
        val jtc = find(configs, "jtc")
        Assert.assertTrue(jtc!!.isJTC)
        Assert.assertFalse(jtc.ignore())
        assertIntegerEquals(570, jtc.jtcValue())
        Assert.assertEquals("ms", jtc.jtcUnit())
        val weird = find(configs, "weird")
        Assert.assertTrue(weird!!.ignore())
    }

    @Test
    fun shouldReadInfectionConfigs() {
        val configs = database!!.infectionConfigs()
        Assert.assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "YES lung abces")
        Assert.assertEquals("Lung abscess", config1!!.interpretation())
        val config2 = find(configs, "NA")
        Assert.assertEquals("No", config2!!.interpretation())
    }

    @Test
    fun shouldReadComplicationConfigs() {
        val configs = database!!.complicationConfigs()
        Assert.assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "something")
        Assert.assertFalse(config1!!.ignore())
        Assert.assertFalse(config1.impliesUnknownComplicationState())
        val curated1 = config1.curated()
        Assert.assertNotNull(curated1)
        Assert.assertEquals("curated something", curated1!!.name())
        Assert.assertEquals(2, curated1.categories().size.toLong())
        assertIntegerEquals(2000, curated1.year())
        assertIntegerEquals(1, curated1.month())
        val config2 = find(configs, "unknown")
        Assert.assertFalse(config2!!.ignore())
        Assert.assertTrue(config2.impliesUnknownComplicationState())
        val curated2 = config2.curated()
        Assert.assertNotNull(curated2)
        Assert.assertEquals(Strings.EMPTY, curated2!!.name())
        Assert.assertEquals(0, curated2.categories().size.toLong())
        Assert.assertNull(curated2.year())
        Assert.assertNull(curated2.month())
    }

    @Test
    fun shouldReadToxicityConfigs() {
        val configs = database!!.toxicityConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = configs[0]
        Assert.assertEquals("Neuropathy GR3", config!!.input())
        Assert.assertEquals("Neuropathy", config.name())
        Assert.assertEquals(Sets.newHashSet("Neuro"), config.categories())
        assertIntegerEquals(3, config.grade())
    }

    @Test
    fun shouldReadMolecularTestConfigs() {
        val configs = database!!.molecularTestConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = configs[0]
        Assert.assertEquals("IHC ERBB2 3+", config!!.input())
        Assert.assertEquals(
            ImmutablePriorMolecularTest.builder()
                .test("IHC")
                .item("ERBB2")
                .measure(null)
                .scoreText(null)
                .scoreValuePrefix(null)
                .scoreValue(3.0)
                .scoreValueUnit("+")
                .impliesPotentialIndeterminateStatus(false)
                .build(), config.curated()
        )
    }

    @Test
    fun shouldReadMedicationNameConfigs() {
        val configs = database!!.medicationNameConfigs()
        Assert.assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "A en B")
        Assert.assertEquals("A and B", config1!!.name())
        Assert.assertFalse(config1.ignore())
        val config2 = find(configs, "No medication")
        Assert.assertTrue(config2!!.ignore())
    }

    @Test
    fun shouldReadMedicationDosageConfigs() {
        val configs = database!!.medicationDosageConfigs()
        Assert.assertEquals(2, configs.size.toLong())
        val config1 = find(configs, "once per day 50-60 mg")
        assertDoubleEquals(50.0, config1!!.dosageMin())
        assertDoubleEquals(60.0, config1.dosageMax())
        Assert.assertEquals("mg", config1.dosageUnit())
        assertDoubleEquals(1.0, config1.frequency())
        Assert.assertEquals("day", config1.frequencyUnit())
        Assert.assertEquals(Boolean.FALSE, config1.ifNeeded())
        val config2 = find(configs, "empty")
        Assert.assertNull(config2!!.dosageMin())
        Assert.assertNull(config2.dosageMax())
        Assert.assertNull(config2.dosageUnit())
        Assert.assertNull(config2.frequency())
        Assert.assertNull(config2.frequencyUnit())
        Assert.assertNull(config2.ifNeeded())
    }

    @Test
    fun shouldReadMedicationCategoryConfigs() {
        val configs = database!!.medicationCategoryConfigs()
        Assert.assertEquals(2, configs.size.toLong())
        val paracetamol = find(configs, "Paracetamol")
        Assert.assertEquals(Sets.newHashSet("Acetanilide derivatives"), paracetamol!!.categories())
        val formoterol = find(configs, "Formoterol and budesonide")
        Assert.assertEquals(Sets.newHashSet("Beta2 sympathomimetics", "Corticosteroids"), formoterol!!.categories())
    }

    @Test
    fun shouldReadAllergyConfigs() {
        val configs = database!!.intoleranceConfigs()
        Assert.assertEquals(1, configs.size.toLong())
        val config = find(configs, "Clindamycine")
        Assert.assertEquals("Clindamycin", config!!.name())
        Assert.assertEquals(Sets.newHashSet("0060500"), config.doids())
    }

    @Test
    fun shouldReadAdministrationRouteTranslations() {
        val translations = database!!.administrationRouteTranslations()
        Assert.assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        Assert.assertEquals("ORAAL", translation!!.administrationRoute())
        Assert.assertEquals("Oral", translation.translatedAdministrationRoute())
    }

    @Test
    fun shouldReadLaboratoryTranslations() {
        val translations = database!!.laboratoryTranslations()
        Assert.assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        Assert.assertEquals("AC", translation!!.code())
        Assert.assertEquals("AC2", translation.translatedCode())
        Assert.assertEquals("ACTH", translation.name())
        Assert.assertEquals("Adrenocorticotropic hormone", translation.translatedName())
    }

    @Test
    fun shouldReadToxicityTranslations() {
        val translations = database!!.toxicityTranslations()
        Assert.assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        Assert.assertEquals("Pijn", translation!!.toxicity())
        Assert.assertEquals("Pain", translation.translatedToxicity())
    }

    @Test
    fun shouldReadBloodTransfusionTranslations() {
        val translations = database!!.bloodTransfusionTranslations()
        Assert.assertEquals(1, translations.size.toLong())
        val translation = translations[0]
        Assert.assertEquals("Thrombocytenconcentraat", translation!!.product())
        Assert.assertEquals("Thrombocyte concentrate", translation.translatedProduct())
    }

    private fun assertIntegerEquals(expected: Int, actual: Int?) {
        Assert.assertNotNull(actual)
        Assert.assertEquals(expected.toLong(), (actual as Int).toLong())
    }

    private fun assertDoubleEquals(expected: Double, actual: Double?) {
        Assert.assertNotNull(actual)
        Assert.assertEquals(expected, actual!!, EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private val CURATION_DIRECTORY = Resources.getResource("curation").path
        private fun <T : CurationConfig?> find(configs: List<T>, input: String): T {
            for (config in configs) {
                if (config!!.input() == input) {
                    return config
                }
            }
            throw IllegalStateException("Could not find input '$input' in configs")
        }
    }
}