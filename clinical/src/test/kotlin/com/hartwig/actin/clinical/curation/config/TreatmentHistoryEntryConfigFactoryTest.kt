package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryConfigFactoryTest {

    @Test
    fun `Should return validation error for treatment history entry config for unknown treatment`() {
        val input = "Unknown therapy 2022"
        val treatmentName = "Unknown therapy"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2022"
            )
        )
        val config = factory.create(fields, parts)
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ONCOLOGICAL_HISTORY.categoryName,
                input,
                "treatmentName",
                "UNKNOWN_THERAPY",
                "treatment",
                "Treatment with name UNKNOWN_THERAPY does not exist in database. Please add with one of the following templates: " +
                        "[{\"name\":\"UNKNOWN_THERAPY\",\"drugs\":[],\"synonyms\":[],\"isSystemic\":?,\"treatmentClass\":\"DRUG_TREATMENT\"}, {\"name\":\"UNKNOWN_THERAPY\"," +
                        "\"synonyms\":[],\"isSystemic\":?,\"radioType\":null,\"isInternal\":null,\"treatmentClass\":\"RADIOTHERAPY\"}, {\"name\":\"UNKNOWN_THERAPY\",\"isSystemic\":?," +
                        "\"synonyms\":[],\"categories\":[],\"types\":[],\"treatmentClass\":\"OTHER_TREATMENT\"}]"
            )
        )
    }

    @Test
    fun `Should generate treatment history entry for known treatment`() {
        val input = "CAPOX 2023"
        val treatmentName = TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2023",
            )
        )

        val config = factory.create(fields, parts).config
        assertThat(config.input).isEqualTo(input)
        assertThat(config.ignore).isFalse
        assertThat(config.curated).isNotNull

        val treatments = config.curated!!.treatments
        assertThat(treatments).containsExactly(treatmentDatabase.findTreatmentByName(treatmentName))
    }

    @Test
    fun `Should generate history entry for unnamed trial with no treatment`() {
        val input = "Unknown trial"
        val treatmentName = ""
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2022",
                "isTrial" to "1"
            )
        )
        val config = factory.create(fields, parts)

        val expected = TreatmentHistoryEntryConfig(
            input = input, ignore = false,
            curated = TreatmentTestFactory.treatmentHistoryEntry(
                startYear = 2022,
                isTrial = true
            ).copy(
                treatmentHistoryDetails = TreatmentHistoryDetails(
                    stopReason = null,
                    bestResponse = null,
                    stopYear = null,
                    stopMonth = null,
                    cycles = null,
                    switchToTreatments = null,
                    maintenanceTreatment = null,
                    stopReasonDetail = null,
                    ongoingAsOf = null
                ),
                intents = null
            )
        )

        assertThat(config.config).isEqualTo(expected)
        assertThat(config.errors).isEmpty()
    }

    @Test
    fun `Should not generate entry for trial with named treatment that does not exist`() {
        val input = "known trial"
        val treatmentName = "trial name"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2022",
                "isTrial" to "1"
            )
        )
        val config = factory.create(fields, parts)
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ONCOLOGICAL_HISTORY.categoryName,
                input,
                "treatmentName",
                "TRIAL_NAME",
                "treatment",
                "Treatment with name TRIAL_NAME does not exist in database. Please add with one of the following templates: " +
                        "[{\"name\":\"TRIAL_NAME\",\"drugs\":[],\"synonyms\":[],\"isSystemic\":?,\"treatmentClass\":\"DRUG_TREATMENT\"}, {\"name\":\"TRIAL_NAME\"," +
                        "\"synonyms\":[],\"isSystemic\":?,\"radioType\":null,\"isInternal\":null,\"treatmentClass\":\"RADIOTHERAPY\"}, {\"name\":\"TRIAL_NAME\"," +
                        "\"isSystemic\":?,\"synonyms\":[],\"categories\":[],\"types\":[],\"treatmentClass\":\"OTHER_TREATMENT\"}]"
            )
        )
    }

    @Test
    fun `Should generate an entry with no curated treatment when treatment name is ignore`() {
        val input = "NA"
        val treatmentName = "<ignore>"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName
            )
        )
        val config = factory.create(fields, parts).config
        assertThat(config.input).isEqualTo("NA")
        assertThat(config.ignore).isTrue
        assertThat(config.curated).isNull()
    }

    @Test
    fun `Should generate treatment history entry for multiple known treatments`() {
        val input = "CAPOX and radiotherapy 2023"
        val treatmentNames = setOf(TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN, "RADIOTHERAPY")
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentNames.joinToString(";"),
                "startYear" to "2023",
            )
        )

        val config = factory.create(fields, parts).config
        assertThat(config.input).isEqualTo(input)
        assertThat(config.ignore).isFalse
        assertThat(config.curated).isNotNull

        val treatments = config.curated!!.treatments
        assertThat(treatments).hasSize(2)
            .isEqualTo(treatmentNames.mapNotNull(treatmentDatabase::findTreatmentByName).toSet())
    }

    @Test
    fun `Should generate treatment history entry with switch to other treatment and maintenance treatment`() {
        val input = "Radiotherapy 2023 with switch to CAPOX 2 cycles 4/2023 and maintenance ablation 8/2023"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to TestTreatmentDatabaseFactory.RADIOTHERAPY,
                "startYear" to "2023",
                "maintenanceTreatment" to TestTreatmentDatabaseFactory.ABLATION,
                "maintenanceTreatmentStartYear" to "2023",
                "maintenanceTreatmentStartMonth" to "8",
                "switchToTreatment" to TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN,
                "switchToTreatmentStartYear" to "2023",
                "switchToTreatmentStartMonth" to "4",
                "switchToTreatmentCycles" to "2"
            )
        )

        val config = factory.create(fields, parts)
        assertThat(config.config.input).isEqualTo(input)
        assertThat(config.config.ignore).isFalse
        val curated = config.config.curated
        assertThat(curated).isNotNull

        val treatments = curated!!.treatments
        assertThat(treatments).containsExactly(treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.RADIOTHERAPY))
        val treatmentDetails = curated.treatmentHistoryDetails!!
        assertThat(treatmentDetails.switchToTreatments).containsExactly(
            TreatmentStage(
                treatment = treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN)!!,
                startYear = 2023,
                startMonth = 4,
                cycles = 2
            )
        )
        assertThat(treatmentDetails.maintenanceTreatment).isEqualTo(
            TreatmentStage(
                treatment = treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.ABLATION)!!,
                startYear = 2023,
                startMonth = 8,
                cycles = null
            )
        )
    }

    private fun partsWithMappedValues(overrides: Map<String, String>): Array<String> {
        val emptyMap = (0..24).associateWith { "" }
        val overridesWithIntKeys = overrides.mapKeys { fields[it.key] }
        return (emptyMap + overridesWithIntKeys).values.toTypedArray()
    }

    companion object {
        private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        private val fields = mapOf(
            "input" to 0,
            "name" to 1,
            "treatmentName" to 2,
            "bodyLocations" to 3,
            "bodyLocationCategories" to 4,
            "intents" to 5,
            "startYear" to 6,
            "startMonth" to 7,
            "stopYear" to 8,
            "stopMonth" to 9,
            "cycles" to 10,
            "bestResponse" to 11,
            "stopReason" to 12,
            "isTrial" to 13,
            "trialAcronym" to 14,
            "maintenanceTreatment" to 15,
            "maintenanceTreatmentStartYear" to 16,
            "maintenanceTreatmentStartMonth" to 17,
            "switchToTreatment" to 18,
            "switchToTreatmentStartYear" to 19,
            "switchToTreatmentStartMonth" to 20,
            "switchToTreatmentCycles" to 21
        )

        private val factory = TreatmentHistoryEntryConfigFactory(treatmentDatabase)
    }
}