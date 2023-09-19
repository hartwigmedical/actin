package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TestTreatmentDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryConfigFactoryTest {

    @Test
    fun shouldNotGenerateTreatmentHistoryEntryConfigForUnknownTreatment() {
        val input = "Unknown therapy 2022"
        val treatmentName = "Unknown therapy"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2022"
            )
        )
        assertThat(TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)).isNull()
    }

    @Test
    fun shouldGenerateTreatmentForKnownTreatment() {
        val input = "CAPOX 2023"
        val treatmentName = TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2023",
            )
        )

        val config = TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)
        assertThat(config).isNotNull
        assertThat(config!!.input).isEqualTo(input)
        assertThat(config.ignore).isFalse
        assertThat(config.curated).isNotNull

        val treatments = config.curated!!.treatments()
        assertThat(treatments).isNotNull
            .containsExactly(treatmentDatabase.findTreatmentByName(treatmentName))
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
        val config = TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)
        assertThat(config).isNotNull()
        assertThat(config?.input).isEqualTo(input)
        assertThat(config?.ignore).isFalse()
        assertThat(config?.curated).isNotNull()
        assertThat(config?.curated?.treatments()).isNotNull()
        assertThat(config?.curated?.treatments()?.size).isEqualTo(0)
        assertThat(config?.curated?.startYear()).isEqualTo(2022)
        assertThat(config?.curated?.startMonth()).isNull()
        assertThat(config?.curated?.intents()).isNull()
        assertThat(config?.curated?.isTrial).isTrue()
        assertThat(config?.curated?.trialAcronym()).isNull()
        assertThat(config?.curated?.therapyHistoryDetails()).isNull()
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
        assertThat(TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)).isNull()
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
        val config = TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)
        assertThat(config).isNotNull()
        assertThat(config?.input).isEqualTo("NA")
        assertThat(config?.ignore).isTrue()
        assertThat(config?.curated).isNull()
    }

    private fun partsWithMappedValues(overrides: Map<String, String>): List<String> {
        val emptyMap = (0..24).associateWith { "" }
        val overridesWithIntKeys = overrides.mapKeys { fields[it.key] }
        return (emptyMap + overridesWithIntKeys).values.toList()
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
    }
}