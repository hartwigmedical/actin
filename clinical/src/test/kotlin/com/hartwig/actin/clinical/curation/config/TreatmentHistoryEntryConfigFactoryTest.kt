package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test

class TreatmentHistoryEntryConfigFactoryTest {

    @Test
    fun shouldNotGenerateTreatmentForCurationWithTherapyCategories() {
        val input = "Unknown therapy 2022"
        val treatmentName = "Unknown therapy"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2022",
                "category" to "Targeted therapy",
                "isSystemic" to "1"
            )
        )
        assertNoGeneratedTreatment(treatmentName, parts)
    }

    @Test
    fun shouldNotGenerateTreatmentForCurationWithIsSystemicUnknown() {
        val input = "Unknown ablation 2023"
        val treatmentName = "Unknown ablation"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2023",
                "category" to "Ablation"
            )
        )
        assertNoGeneratedTreatment(treatmentName, parts)
    }

    @Test
    fun shouldGenerateTreatmentForCurationWithIsSystemicProvided() {
        val input = "Unknown ablation 2023"
        val treatmentName = "Unknown ablation"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2023",
                "category" to "Ablation",
                "isSystemic" to "0"
            )
        )
        assertGeneratedTreatment(treatmentName, parts, input, TreatmentCategory.ABLATION)
    }

    @Test
    fun shouldGenerateTreatmentForSurgeryCurationWithIsSystemicUnknown() {
        val input = "Unknown surgery 2023"
        val treatmentName = "Unknown surgery"
        val parts = partsWithMappedValues(
            mapOf(
                "input" to input,
                "treatmentName" to treatmentName,
                "startYear" to "2023",
                "category" to "Surgery"
            )
        )
        assertGeneratedTreatment(treatmentName, parts, input, TreatmentCategory.SURGERY)
    }

    private fun assertNoGeneratedTreatment(treatmentName: String, parts: List<String>) {
        assertThat(TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)).isNull()
    }

    private fun assertGeneratedTreatment(
        treatmentName: String,
        parts: List<String>,
        input: String,
        treatmentCategory: TreatmentCategory
    ) {
        val config = TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)
        assertThat(config).isNotNull
        assertThat(config!!.input).isEqualTo(input)
        assertThat(config.ignore).isFalse
        assertThat(config.curated).isNotNull
        val treatments = config.curated!!.treatments()
        assertThat(treatments).extracting("name", "categories", "isSystemic", "types")
            .containsExactly(tuple(treatmentName, setOf(treatmentCategory), false, emptySet<TreatmentType>()))
    }

    private fun partsWithMappedValues(overrides: Map<String, String>): List<String> {
        val emptyMap = (0..24).associateWith { "" }
        val overridesWithIntKeys = overrides.mapKeys { fields[it.key] }
        return (emptyMap + overridesWithIntKeys).values.toList()
    }

    companion object {
        private val treatmentDatabase = TreatmentDatabase(emptyMap(), emptyMap())
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
            "category" to 13,
            "isSystemic" to 14,
            "chemoType" to 15,
            "immunoType" to 16,
            "targetedType" to 17,
            "hormoneType" to 18,
            "radioType" to 19,
            "carTType" to 20,
            "transplantType" to 21,
            "supportiveType" to 22,
            "trialAcronym" to 23,
            "ablationType" to 24
        )
    }
}