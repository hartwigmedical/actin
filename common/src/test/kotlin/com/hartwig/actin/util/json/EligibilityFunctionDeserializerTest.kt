package com.hartwig.actin.util.json

import com.google.gson.GsonBuilder
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.serialization.TreatmentAdapter
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.SystemicTreatmentParameter
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.trial.input.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFunctionDeserializerTest {

    @Test
    fun `Should deserialize eligibility function without modification`() {
        val function = EligibilityFunction(
            rule = EligibilityRule.NOT.name, parameters = listOf(
                FunctionParameter(
                    EligibilityFunction(rule = EligibilityRule.HAS_ACTIVE_BRAIN_METASTASES.name, parameters = emptyList())
                )
            )
        )
        val gson = GsonBuilder().registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer()).create()

        assertThat(gson.fromJson(gson.toJson(function), EligibilityFunction::class.java)).isEqualTo(function)
    }

    @Test
    fun `Should deserialize treatment and drug parameters with full objects`() {
        val treatmentDb = TestTreatmentDatabaseFactory.createProper()
        val treatment = requireNotNull(treatmentDb.findTreatmentByName(TestTreatmentDatabaseFactory.CISPLATIN))
        val drug = requireNotNull(treatmentDb.findDrugByName(TestTreatmentDatabaseFactory.CISPLATIN))

        val function = EligibilityFunction(
            rule = "CUSTOM_RULE",
            parameters = listOf(
                TreatmentParameter(treatment),
                SystemicTreatmentParameter(treatment),
                DrugParameter(drug),
                ManyTreatmentsParameter(listOf(treatment)),
                ManyDrugsParameter(setOf(drug))
            )
        )

        val gson = GsonBuilder()
            .registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
            .registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer())
            .create()

        assertThat(gson.fromJson(gson.toJson(function), EligibilityFunction::class.java)).isEqualTo(function)
    }
}
