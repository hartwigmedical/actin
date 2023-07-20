package com.hartwig.actin.clinical.curation

import com.google.common.io.Resources
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.Assertions.within
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CurationDatabaseReaderTest {
    private val reader = CurationDatabaseReader(
        TestCurationFactory.createMinimalTestCurationDatabaseValidator(),
        TreatmentDatabaseFactory.createFromPath(TREATMENT_DIRECTORY)
    )
    private var database: CurationDatabase? = null

    @Before
    @Throws(IOException::class)
    fun createDatabase() {
        database = reader.read(CURATION_DIRECTORY)
    }

    @Test
    fun shouldReadPrimaryTumorConfigs() {
        val configs = database!!.primaryTumorConfigs
        assertThat(configs).hasSize(1)
        val config = configs[0]
        assertThat(config.input).isEqualTo("Unknown | Carcinoma")
        assertThat(config.primaryTumorLocation).isEqualTo("Unknown")
        assertThat(config.primaryTumorSubLocation).isEqualTo("CUP")
        assertThat(config.primaryTumorType).isEqualTo("Carcinoma")
        assertThat(config.primaryTumorSubType).isEqualTo(Strings.EMPTY)
        assertThat(config.primaryTumorExtraDetails).isEqualTo(Strings.EMPTY)
        assertThat(config.doids).hasSize(1)
        assertThat(config.doids).contains("299")
    }

    @Test
    fun shouldReadOncologicalHistoryConfigs() {
        val configs = database!!.oncologicalHistoryConfigs
        assertThat(configs).hasSize(3)

        val capoxConfig = find(configs, "Capecitabine/Oxaliplatin 2020-2021")
        assertThat(capoxConfig.ignore).isFalse
        assertThat(capoxConfig.curated).isEqualTo(
            ImmutablePriorTumorTreatment.builder()
                .name("Capecitabine+Oxaliplatin")
                .startYear(2020)
                .stopYear(2021)
                .cycles(6)
                .bestResponse("PR")
                .stopReason("toxicity")
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .isSystemic(true)
                .chemoType("antimetabolite,platinum")
                .build()
        )

        val appendectomyConfig = find(configs, "2022 appendectomy")
        assertThat(appendectomyConfig.ignore).isFalse
        val curatedAppendectomy = appendectomyConfig.curated
        assertThat(curatedAppendectomy).isNotNull
        assertThat(curatedAppendectomy!!.name()).isEqualTo("Appendectomy")
        assertThat(curatedAppendectomy.startYear()).isEqualTo(2022)
        assertThat(curatedAppendectomy.categories()).containsExactly(TreatmentCategory.SURGERY)
        assertThat(curatedAppendectomy.isSystemic).isFalse

        val ablationConfig = find(configs, "Ablation trial 2023 May")
        assertThat(ablationConfig.ignore).isFalse
        val curatedAblation = ablationConfig.curated
        assertThat(curatedAblation).isNotNull
        assertThat(curatedAblation!!.name()).isEqualTo("Ablation trial")
        assertThat(curatedAblation.startYear()).isEqualTo(2023)
        assertThat(curatedAblation.startMonth()).isEqualTo(5)
        assertThat(curatedAblation.categories()).containsExactlyInAnyOrder(TreatmentCategory.ABLATION, TreatmentCategory.TRIAL)
        assertThat(curatedAblation.isSystemic).isFalse
    }

    @Test
    fun shouldReadTreatmentHistoryConfigs() {
        val configs = database!!.treatmentHistoryEntryConfigs
        assertThat(configs).hasSize(3)

        val capoxConfig = find(configs, "Capecitabine/Oxaliplatin 2020-2021")
        assertThat(capoxConfig.ignore).isFalse

        val curatedCapox = capoxConfig.curated
        assertThat(curatedCapox).isNotNull
        assertThat(curatedCapox!!.treatments()).hasSize(1)

        val capoxTreatment = curatedCapox.treatments().iterator().next() as Therapy
        assertThat(capoxTreatment.name()).isEqualTo("CAPECITABINE+OXALIPLATIN")
        assertThat(curatedCapox.startYear()).isEqualTo(2020)
        assertThat(curatedCapox.startMonth()).isNull()

        val therapyHistoryDetails = curatedCapox.therapyHistoryDetails()
        assertThat(therapyHistoryDetails!!.stopYear()).isEqualTo(2021)
        assertThat(therapyHistoryDetails.stopMonth()).isNull()
        assertThat(therapyHistoryDetails.cycles()).isEqualTo(6)
        assertThat(therapyHistoryDetails.bestResponse()).isEqualTo(TreatmentResponse.PARTIAL_RESPONSE)
        assertThat(therapyHistoryDetails.stopReason()).isEqualTo(StopReason.TOXICITY)

        assertThat(capoxTreatment.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY)
        assertThat(capoxTreatment.isSystemic).isTrue
        assertThat(capoxTreatment.drugs()).extracting(Drug::name, Drug::drugTypes).containsExactlyInAnyOrder(
            tuple("CAPECITABINE", setOf(DrugType.ANTIMETABOLITE)),
            tuple("OXALIPLATIN", setOf(DrugType.PLATINUM_COMPOUND))
        )
        assertThat(curatedCapox.trialAcronym()).isNull()

        val appendectomyConfig = find(configs, "2022 appendectomy")
        assertThat(appendectomyConfig.ignore).isFalse
        val curatedAppendectomy = appendectomyConfig.curated
        assertThat(curatedAppendectomy).isNotNull
        assertThat(curatedAppendectomy!!.startYear()).isEqualTo(2022)
        val appendectomyTreatment = curatedAppendectomy.treatments().iterator().next()
        assertThat(appendectomyTreatment!!.name()).isEqualTo("Appendectomy")
        assertThat(appendectomyTreatment.categories()).containsExactly(TreatmentCategory.SURGERY)
        assertThat(appendectomyTreatment.isSystemic).isFalse

        val ablationConfig = find(configs, "Ablation trial 2023 May")
        assertThat(ablationConfig.ignore).isFalse
        val curatedAblation = ablationConfig.curated
        assertThat(curatedAblation).isNotNull
        assertThat(curatedAblation!!.startYear()).isEqualTo(2023)
        assertThat(curatedAblation.startMonth()).isEqualTo(5)
        assertThat(curatedAblation.isTrial).isTrue
        val ablationTreatment = curatedAblation.treatments().iterator().next()
        assertThat(ablationTreatment.name()).isEqualTo("Ablation")
        assertThat(ablationTreatment.categories()).containsExactly(TreatmentCategory.ABLATION)
        assertThat(ablationTreatment.isSystemic).isFalse
    }

    @Test
    fun shouldReadSecondPrimaryConfigs() {
        val configs = database!!.secondPrimaryConfigs
        assertThat(configs).hasSize(1)

        val config = find(configs, "basaalcelcarcinoom (2014) | 2014")
        assertThat(config.ignore).isFalse

        val curated = config.curated
        assertThat(curated).isNotNull
        assertThat(curated!!.tumorLocation()).isEqualTo("")
        assertThat(curated.tumorSubLocation()).isEqualTo("")
        assertThat(curated.tumorType()).isEqualTo("Carcinoma")
        assertThat(curated.tumorSubType()).isEqualTo("Basal cell carcinoma")
        assertThat(curated.doids()).containsExactly("2513")
        assertThat(curated.diagnosedYear()).isEqualTo(2014)
        assertThat(curated.diagnosedMonth()).isEqualTo(1)
        assertThat(curated.treatmentHistory()).isEqualTo("None")
        assertThat(curated.lastTreatmentYear()).isEqualTo(2014)
        assertThat(curated.lastTreatmentMonth()).isEqualTo(2)
        assertThat(curated.isActive).isFalse
    }

    @Test
    fun shouldReadLesionLocationConfigs() {
        val configs = database!!.lesionLocationConfigs
        assertThat(configs).hasSize(1)
        val config = configs[0]
        assertThat(config.input).isEqualTo("Lever")
        assertThat(config.location).isEqualTo("Liver")
    }

    @Test
    fun shouldReadNonOncologicalHistoryConfigs() {
        val configs = database!!.nonOncologicalHistoryConfigs
        assertThat(configs).hasSize(4)

        val config1 = find(configs, "Levercirrose/ sarcoidose")
        assertThat(config1.ignore).isFalse
        assertThat(config1.priorOtherCondition.isPresent).isTrue
        assertThat(config1.lvef.isPresent).isFalse

        val curated1 = config1.priorOtherCondition.get()
        assertThat(curated1.name()).isEqualTo("Liver cirrhosis and sarcoidosis")
        assertThat(curated1.year()).isEqualTo(2019)
        assertThat(curated1.month()).isEqualTo(7)
        assertThat(curated1.category()).isEqualTo("Liver disease")
        assertThat(curated1.doids()).containsExactlyInAnyOrder("5082", "11335")
        assertThat(curated1.isContraindicationForTherapy).isFalse

        val config2 = find(configs, "NA")
        assertThat(config2.ignore).isTrue
        assertThat(config2.lvef.isPresent).isFalse
        assertThat(config2.priorOtherCondition.isPresent).isFalse

        val config3 = find(configs, "LVEF 0.17")
        assertThat(config3.ignore).isFalse
        assertThat(config3.lvef.isPresent).isTrue
        assertThat(config3.priorOtherCondition.isPresent).isFalse
        assertDoubleEquals(0.17, config3.lvef.get())

        val config4 = find(configs, "No contraindication")
        assertThat(config4.priorOtherCondition.isPresent).isTrue
        val curated4 = config4.priorOtherCondition.get()
        assertThat(curated4.isContraindicationForTherapy).isTrue
    }

    @Test
    fun shouldReadECGConfigs() {
        val configs = database!!.ecgConfigs
        assertThat(configs).hasSize(4)
        val sinus = find(configs, "Sinus Tachycardia")
        assertThat(sinus.interpretation).isEqualTo("Sinus tachycardia")
        assertThat(sinus.ignore).isFalse
        assertThat(sinus.isQTCF).isFalse
        assertThat(sinus.qtcfValue).isNull()
        assertThat(sinus.qtcfUnit).isNull()
        assertThat(sinus.isJTC).isFalse
        assertThat(sinus.jtcValue).isNull()
        assertThat(sinus.jtcUnit).isNull()
        val qtcf = find(configs, "qtcf")
        assertThat(qtcf.isQTCF).isTrue
        assertThat(qtcf.ignore).isFalse
        assertThat(qtcf.qtcfValue).isEqualTo(470)
        assertThat(qtcf.qtcfUnit).isEqualTo("ms")
        val jtc = find(configs, "jtc")
        assertThat(jtc.isJTC).isTrue
        assertThat(jtc.ignore).isFalse
        assertThat(jtc.jtcValue).isEqualTo(570)
        assertThat(jtc.jtcUnit).isEqualTo("ms")
        val weird = find(configs, "weird")
        assertThat(weird.ignore).isTrue
    }

    @Test
    fun shouldReadInfectionConfigs() {
        val configs = database!!.infectionConfigs
        assertThat(configs).hasSize(2)
        val config1 = find(configs, "YES lung abces")
        assertThat(config1.interpretation).isEqualTo("Lung abscess")
        val config2 = find(configs, "NA")
        assertThat(config2.interpretation).isEqualTo("No")
    }

    @Test
    fun shouldReadPeriodBetweenUnitConfigs() {
        val configs = database!!.periodBetweenUnitConfigs
        assertThat(configs.size.toLong()).isEqualTo(1)
        val config1 = find(configs, "mo")
        assertThat(config1.interpretation).isEqualTo("months")
    }

    @Test
    fun shouldReadComplicationConfigs() {
        val configs = database!!.complicationConfigs
        assertThat(configs).hasSize(2)

        val config1 = find(configs, "something")
        assertThat(config1.ignore).isFalse
        assertThat(config1.impliesUnknownComplicationState).isFalse

        val curated1 = config1.curated
        assertThat(curated1).isNotNull
        assertThat(curated1!!.name()).isEqualTo("curated something")
        assertThat(curated1.categories().size.toLong()).isEqualTo(2)
        assertThat(curated1.year()).isEqualTo(2000)
        assertThat(curated1.month()).isEqualTo(1)

        val config2 = find(configs, "unknown")
        assertThat(config2.ignore).isFalse
        assertThat(config2.impliesUnknownComplicationState).isTrue

        val curated2 = config2.curated
        assertThat(curated2).isNotNull
        assertThat(curated2!!.name()).isEqualTo(Strings.EMPTY)
        assertThat(curated2.categories().size.toLong()).isEqualTo(0)
        assertThat(curated2.year()).isNull()
        assertThat(curated2.month()).isNull()
    }

    @Test
    fun shouldReadToxicityConfigs() {
        val configs = database!!.toxicityConfigs
        assertThat(configs).hasSize(1)
        val config = configs[0]
        assertThat(config.input).isEqualTo("Neuropathy GR3")
        assertThat(config.name).isEqualTo("Neuropathy")
        assertThat(config.categories).containsExactly("Neuro")
        assertThat(config.grade).isEqualTo(3)
    }

    @Test
    fun shouldReadMolecularTestConfigs() {
        val configs = database!!.molecularTestConfigs
        assertThat(configs).hasSize(1)
        val config = configs[0]
        assertThat(config.input).isEqualTo("IHC ERBB2 3+")
        assertThat(config.curated).isEqualTo(
            ImmutablePriorMolecularTest.builder()
                .test("IHC")
                .item("ERBB2")
                .measure(null)
                .scoreText(null)
                .scoreValuePrefix(null)
                .scoreValue(3.0)
                .scoreValueUnit("+")
                .impliesPotentialIndeterminateStatus(false)
                .build()
        )
    }

    @Test
    fun shouldReadMedicationNameConfigs() {
        val configs = database!!.medicationNameConfigs
        assertThat(configs).hasSize(2)
        val config1 = find(configs, "A en B")
        assertThat(config1.name).isEqualTo("A and B")
        assertThat(config1.ignore).isFalse
        val config2 = find(configs, "No medication")
        assertThat(config2.ignore).isTrue
    }

    @Test
    fun shouldReadMedicationDosageConfigs() {
        val configs = database!!.medicationDosageConfigs
        assertThat(configs).hasSize(1)
        val config1 = find(configs, "once per day 50-60 mg every month")
        assertDoubleEquals(50.0, config1.dosageMin)
        assertDoubleEquals(60.0, config1.dosageMax)
        assertThat(config1.dosageUnit).isEqualTo("mg")
        assertDoubleEquals(1.0, config1.frequency)
        assertThat(config1.frequencyUnit).isEqualTo("day")
        assertThat(config1.periodBetweenValue).isEqualTo(0.0)
        assertThat(config1.periodBetweenUnit).isEqualTo("mo")
        assertThat(config1.ifNeeded).isEqualTo(false)
    }

    @Test
    fun shouldReadMedicationCategoryConfigs() {
        val configs = database!!.medicationCategoryConfigs
        assertThat(configs).hasSize(2)
        val paracetamol = find(configs, "Paracetamol")
        assertThat(paracetamol.categories).containsExactly("Acetanilide derivatives")
        val formoterol = find(configs, "Formoterol and budesonide")
        assertThat(formoterol.categories).containsExactlyInAnyOrder("Beta2 sympathomimetics", "Corticosteroids")
    }

    @Test
    fun shouldReadAllergyConfigs() {
        val configs = database!!.intoleranceConfigs
        assertThat(configs).hasSize(1)
        val config = find(configs, "Clindamycine")
        assertThat(config.name).isEqualTo("Clindamycin")
        assertThat(config.doids).containsExactly("0060500")
    }

    @Test
    fun shouldReadDatabaseFromTsvFile() {
        assertThat(database!!.cypInteractionConfigs).containsExactly(
            CypInteractionConfig(
                "abiraterone", false,
                listOf(
                    createInteraction(CypInteraction.Type.INHIBITOR, CypInteraction.Strength.MODERATE, "2D6"),
                    createInteraction(CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.MODERATE_SENSITIVE, "3A4")
                )
            )
        )
    }

    @Test
    fun shouldReadAdministrationRouteTranslations() {
        val translations = database!!.administrationRouteTranslations
        assertThat(translations).hasSize(1)
        val translation = translations[0]
        assertThat(translation.administrationRoute).isEqualTo("ORAAL")
        assertThat(translation.translatedAdministrationRoute).isEqualTo("Oral")
    }

    @Test
    fun shouldReadLaboratoryTranslations() {
        val translations = database!!.laboratoryTranslations
        assertThat(translations).hasSize(1)
        val translation = translations[0]
        assertThat(translation.code).isEqualTo("AC")
        assertThat(translation.translatedCode).isEqualTo("AC2")
        assertThat(translation.name).isEqualTo("ACTH")
        assertThat(translation.translatedName).isEqualTo("Adrenocorticotropic hormone")
    }

    @Test
    fun shouldReadToxicityTranslations() {
        val translations = database!!.toxicityTranslations
        assertThat(translations).hasSize(1)
        val translation = translations[0]
        assertThat(translation.toxicity).isEqualTo("Pijn")
        assertThat(translation.translatedToxicity).isEqualTo("Pain")
    }

    @Test
    fun shouldReadBloodTransfusionTranslations() {
        val translations = database!!.bloodTransfusionTranslations
        assertThat(translations).hasSize(1)
        val translation = translations[0]
        assertThat(translation.product).isEqualTo("Thrombocytenconcentraat")
        assertThat(translation.translatedProduct).isEqualTo("Thrombocyte concentrate")
    }

    @Test
    fun shouldReadDosageUnitTranslations() {
        val translations = database!!.dosageUnitTranslations
        assertThat(translations.size.toLong()).isEqualTo(1)
        val translation = translations[0]
        assertThat(translation.dosageUnit).isEqualTo("stuk")
        assertThat(translation.translatedDosageUnit).isEqualTo("piece")
    }

    private fun assertDoubleEquals(expected: Double, actual: Double?) {
        assertThat(actual).isNotNull.isEqualTo(expected, within(EPSILON))
    }

    private fun createInteraction(type: CypInteraction.Type, strength: CypInteraction.Strength, cyp: String): ImmutableCypInteraction =
        ImmutableCypInteraction.builder().type(type).strength(strength).cyp(cyp).build()

    companion object {
        private const val EPSILON = 1.0E-10
        private val CURATION_DIRECTORY = Resources.getResource("curation").path
        private val TREATMENT_DIRECTORY = Resources.getResource("treatment_db").path
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