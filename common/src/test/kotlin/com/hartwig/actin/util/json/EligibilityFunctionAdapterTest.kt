package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.module.SimpleModule
import com.hartwig.actin.clinical.serialization.TreatmentDeserializer
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.SystemicTreatmentParameter
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.treatment.database.TestTreatmentDatabaseFactory
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.serialization.EligibilityFunctionDeserializer
import com.hartwig.actin.trial.serialization.EligibilityFunctionSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EligibilityFunctionAdapterTest {

    private fun mapper(includeTreatment: Boolean = false) = ActinObjectMapper.create().registerModule(
        SimpleModule().apply {
            addSerializer(EligibilityFunction::class.java, EligibilityFunctionSerializer)
            addDeserializer(EligibilityFunction::class.java, EligibilityFunctionDeserializer)
            if (includeTreatment) {
                addDeserializer(Treatment::class.java, TreatmentDeserializer)
            }
        }
    )

    @Test
    fun `Should round-trip eligibility function`() {
        val function = EligibilityFunction(
            rule = EligibilityRule.NOT.name, parameters = listOf(
                FunctionParameter(
                    EligibilityFunction(rule = EligibilityRule.HAS_ACTIVE_BRAIN_METASTASES.name, parameters = emptyList())
                )
            )
        )
        val mapper = mapper()
        assertThat(mapper.readValue(mapper.writeValueAsString(function), EligibilityFunction::class.java)).isEqualTo(function)
    }

    @Test
    fun `Should round-trip treatment and drug parameters with full objects`() {
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

        val mapper = mapper(includeTreatment = true)
        assertThat(mapper.readValue(mapper.writeValueAsString(function), EligibilityFunction::class.java)).isEqualTo(function)
    }
}
