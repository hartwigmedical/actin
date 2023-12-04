package com.hartwig.actin.clinical.curation

import com.google.common.io.Resources
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(database!!.curate<PrimaryTumorConfig>("Unknown | Carcinoma")).hasSize(1)
    }

    @Test
    fun shouldReadTreatmentHistoryConfigs() {
        assertThat(database!!.curate<TreatmentHistoryEntryConfig>("Capecitabine/Oxaliplatin 2020-2021")).hasSize(1)
    }

    @Test
    fun shouldReadSecondPrimaryConfigs() {
        assertThat(database!!.curate<SecondPrimaryConfig>("basaalcelcarcinoom (2014) | 2014")).hasSize(1)
    }

    @Test
    fun shouldReadLesionLocationConfigs() {
        assertThat(database!!.curate<LesionLocationConfig>("Lever")).hasSize(1)
    }

    @Test
    fun shouldReadNonOncologicalHistoryConfigs() {
        assertThat(database!!.curate<LesionLocationConfig>("Levercirrose/ sarcoidose")).hasSize(1)
    }

    @Test
    fun shouldReadECGConfigs() {
        assertThat(database!!.curate<ECGConfig>("Sinus Tachycardia")).hasSize(1)
    }

    @Test
    fun shouldReadInfectionConfigs() {
        assertThat(database!!.curate<ECGConfig>("YES lung abces")).hasSize(1)
    }

    @Test
    fun shouldReadPeriodBetweenUnitConfigs() {
        assertThat(database!!.curate<PeriodBetweenUnitConfig>("mo")).hasSize(1)
    }

    @Test
    fun shouldReadComplicationConfigs() {
        assertThat(database!!.curate<ComplicationConfig>("something")).hasSize(1)
    }

    @Test
    fun shouldReadToxicityConfigs() {
        assertThat(database!!.curate<ToxicityConfig>("Neuropathy GR3")).hasSize(1)
    }

    @Test
    fun shouldReadMolecularTestConfigs() {
        assertThat(database!!.curate<MolecularTestConfig>("IHC ERBB2 3+")).hasSize(1)
    }

    @Test
    fun shouldReadMedicationNameConfigs() {
        assertThat(database!!.curate<MedicationNameConfig>("A en B")).hasSize(1)
    }

    @Test
    fun shouldReadMedicationDosageConfigs() {
        assertThat(database!!.curate<MedicationDosageConfig>("once per day 50-60 mg every month")).hasSize(1)
    }

    @Test
    fun shouldReadAllergyConfigs() {
        assertThat(database!!.curate<IntoleranceConfig>("Clindamycine")).hasSize(1)
    }

    @Test
    fun shouldReadDatabaseFromTsvFile() {
        assertThat(database!!.curate<CypInteractionConfig>("abiraterone")).containsExactly(
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
        assertThat(database!!.translate("ORAAL")).isEqualTo("Oral")
    }

    @Test
    fun shouldReadLaboratoryTranslations() {
        val translations = database!!.laboratoryTranslations
        assertThat(translations).hasSize(1)
        val translation = translations.values.first()
        assertThat(translation.code).isEqualTo("AC")
        assertThat(translation.translatedCode).isEqualTo("AC2")
        assertThat(translation.name).isEqualTo("ACTH")
        assertThat(translation.translatedName).isEqualTo("Adrenocorticotropic hormone")
    }

    @Test
    fun shouldReadToxicityTranslations() {
        assertThat(database!!.translate("Pijn")).isEqualTo("Pain")
    }

    @Test
    fun shouldReadBloodTransfusionTranslations() {
        assertThat(database!!.translate("Thrombocytenconcentraat")).isEqualTo("Thrombocyte concentrate")
    }

    @Test
    fun shouldReadDosageUnitTranslations() {
        assertThat(database!!.translate("stuk")).isEqualTo("piece")
    }

    private fun createInteraction(type: CypInteraction.Type, strength: CypInteraction.Strength, cyp: String): ImmutableCypInteraction =
        ImmutableCypInteraction.builder().type(type).strength(strength).cyp(cyp).build()

    companion object {
        private val CURATION_DIRECTORY = Resources.getResource("curation").path
        private val TREATMENT_DIRECTORY = Resources.getResource("treatment_db").path
    }
}