package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Assert.assertThrows
import org.junit.Test

class TreatmentHistoryEntryConfigFactoryTest {

    @Test
    fun `Should not generate treatment history entry config for unknown treatment`() {
        val input = "Unknown therapy 2022"
        val treatmentName = "Unknown therapy"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2022"
            )
        )
        assertThat(assertThrows(IllegalStateException::class.java) {
            factory.create(fields, parts)
        }.message).isEqualTo("Treatment with name UNKNOWN_THERAPY does not exist in database. Please add with one of the following templates: \n" +
                "[{\"name\":\"UNKNOWN_THERAPY\",\"synonyms\":[],\"isSystemic\":?,\"drugs\":[],\"treatmentClass\":\"DRUG_TREATMENT\"}, {\"name\":\"UNKNOWN_THERAPY\"," +
                "\"synonyms\":[],\"isSystemic\":?,\"radioType\":null,\"isInternal\":null,\"treatmentClass\":\"RADIOTHERAPY\"}, {\"name\":\"UNKNOWN_THERAPY\",\"categories\":[]," +
                "\"synonyms\":[],\"isSystemic\":?,\"types\":[],\"treatmentClass\":\"OTHER_TREATMENT\"}]")
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

        val config = factory.create(fields, parts)
        assertThat(config.input).isEqualTo(input)
        assertThat(config.ignore).isFalse
        assertThat(config.curated).isNotNull

        val treatments = config.curated!!.treatments()
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
            curated = ImmutableTreatmentHistoryEntry.builder()
                .startYear(2022)
                .isTrial(true)
                .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().build())
                .build()
        )

        assertThat(config).isEqualTo(expected)
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
        assertThat(assertThrows(IllegalStateException::class.java) {
            factory.create(fields, parts)
        }.message).isEqualTo("Treatment with name TRIAL_NAME does not exist in database. Please add with one of the following templates: \n" +
                "[{\"name\":\"TRIAL_NAME\",\"synonyms\":[],\"isSystemic\":?,\"drugs\":[],\"treatmentClass\":\"DRUG_TREATMENT\"}, {\"name\":\"TRIAL_NAME\"," +
                "\"synonyms\":[],\"isSystemic\":?,\"radioType\":null,\"isInternal\":null,\"treatmentClass\":\"RADIOTHERAPY\"}, {\"name\":\"TRIAL_NAME\"," +
                "\"categories\":[],\"synonyms\":[],\"isSystemic\":?,\"types\":[],\"treatmentClass\":\"OTHER_TREATMENT\"}]")
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
        val config = factory.create(fields, parts)
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

        val config = factory.create(fields, parts)
        assertThat(config.input).isEqualTo(input)
        assertThat(config.ignore).isFalse
        assertThat(config.curated).isNotNull

        val treatments = config.curated!!.treatments()
        assertThat(treatments).hasSize(2)
            .isEqualTo(treatmentNames.mapNotNull(treatmentDatabase::findTreatmentByName).toSet())
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
        )

        private val factory = TreatmentHistoryEntryConfigFactory(treatmentDatabase)
    }
}