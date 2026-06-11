package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.module.SimpleModule
import com.hartwig.actin.algo.serialization.EvaluationMessageDeserializer
import com.hartwig.actin.algo.serialization.EvaluationMessageSerializer
import com.hartwig.actin.clinical.serialization.ComorbidityDeserializer
import com.hartwig.actin.clinical.serialization.DrugDeserializer
import com.hartwig.actin.clinical.serialization.TreatmentDeserializer
import com.hartwig.actin.clinical.serialization.WhoStatusDeserializer
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ClinicalAdaptersTest {

    private val mapper = ActinObjectMapper.create().registerModule(
        SimpleModule().apply {
            addDeserializer(Drug::class.java, DrugDeserializer)
            addDeserializer(Treatment::class.java, TreatmentDeserializer)
            addDeserializer(Comorbidity::class.java, ComorbidityDeserializer)
            addDeserializer(WhoStatus::class.java, WhoStatusDeserializer)
            addSerializer(EvaluationMessage::class.java, EvaluationMessageSerializer)
            addDeserializer(EvaluationMessage::class.java, EvaluationMessageDeserializer)
        }
    )

    @Test
    fun `Should round-trip Drug, defaulting missing synonyms to empty set`() {
        val json = """{"name":"Cisplatin","drugTypes":["PLATINUM_COMPOUND"],"category":"CHEMOTHERAPY","displayOverride":null}"""
        val drug = mapper.readValue(json, Drug::class.java)
        assertThat(drug.name).isEqualTo("Cisplatin")
        assertThat(drug.synonyms).isEmpty()
        assertThat(drug.drugTypes).containsExactly(DrugType.PLATINUM_COMPOUND)
        assertThat(drug.category).isEqualTo(TreatmentCategory.CHEMOTHERAPY)
        assertThat(drug.displayOverride).isNull()
    }

    @Test
    fun `Should round-trip DrugTreatment via Treatment polymorphic deserializer`() {
        val treatment = TreatmentTestFactory.drugTreatment(
            name = "FOLFOX",
            category = TreatmentCategory.CHEMOTHERAPY,
            types = setOf(DrugType.PLATINUM_COMPOUND)
        )
        val roundtripped = mapper.readValue(mapper.writeValueAsString(treatment), Treatment::class.java)
        assertThat(roundtripped).isEqualTo(treatment)
    }

    @Test
    fun `Should round-trip OtherCondition via Comorbidity polymorphic deserializer`() {
        val condition: Comorbidity = OtherCondition(name = "Hypertension", icdCodes = emptySet(), year = 2024, month = 6)
        val roundtripped = mapper.readValue(mapper.writeValueAsString(condition), Comorbidity::class.java)
        assertThat(roundtripped).isEqualTo(condition)
    }

    @Test
    fun `Should dispatch Toxicity via Comorbidity polymorphic deserializer`() {
        val toxicity: Comorbidity = Toxicity(
            name = "Nausea",
            icdCodes = emptySet(),
            evaluatedDate = LocalDate.of(2026, 1, 5),
            source = ToxicitySource.EHR,
            grade = 2
        )
        val roundtripped = mapper.readValue(mapper.writeValueAsString(toxicity), Comorbidity::class.java)
        assertThat(roundtripped).isEqualTo(toxicity)
    }

    @Test
    fun `Should default WHO status precision to EXACT when omitted`() {
        val json = """{"date":"2026-06-08","status":2}"""
        val whoStatus = mapper.readValue(json, WhoStatus::class.java)
        assertThat(whoStatus).isEqualTo(WhoStatus(date = LocalDate.of(2026, 6, 8), status = 2, precision = WhoStatusPrecision.EXACT))
    }

    @Test
    fun `Should respect explicit WHO status precision`() {
        val json = """{"date":"2026-06-08","status":2,"precision":"AT_LEAST"}"""
        val whoStatus = mapper.readValue(json, WhoStatus::class.java)
        assertThat(whoStatus.precision).isEqualTo(WhoStatusPrecision.AT_LEAST)
    }

    @Test
    fun `Should collapse EvaluationMessage to StaticMessage on serialize`() {
        val message: EvaluationMessage = StaticMessage("patient is eligible")
        val json = mapper.writeValueAsString(message)
        val roundtripped = mapper.readValue(json, EvaluationMessage::class.java)
        assertThat(roundtripped).isEqualTo(message)
    }
}
