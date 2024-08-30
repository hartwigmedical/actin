package com.hartwig.actin.util.json

import com.google.gson.GsonBuilder
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFunctionDeserializerTest {

    @Test
    fun `Should deserialize eligibility function without modification`() {
        val function = EligibilityFunction(
            rule = EligibilityRule.NOT, parameters = listOf(
                EligibilityFunction(rule = EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES, parameters = emptyList())
            )
        )
        val gson = GsonBuilder().registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer()).create()

        assertThat(gson.fromJson(gson.toJson(function), EligibilityFunction::class.java)).isEqualTo(function)
    }
}