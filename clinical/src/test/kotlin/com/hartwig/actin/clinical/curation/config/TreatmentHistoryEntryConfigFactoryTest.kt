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

    private fun partsWithMappedValues(overrides: Map<String, String>): List<String> {
        val emptyMap = (0..24).associateWith { "" }
        val overridesWithIntKeys = overrides.mapKeys { fields[it.key] }
        return (emptyMap + overridesWithIntKeys).values.toList()
    }

    companion object {
        private val treatmentDatabase = TestTreatmentDatabaseFactory.create()
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