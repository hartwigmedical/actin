package com.hartwig.actin.clinical.ehr

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator
import org.junit.Test


class EhrPatientRecordTest {

    @Test
    fun `Create schema`() {
        val mapper = ObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        val schemaGen = JsonSchemaGenerator(mapper)
        val schema: JsonSchema = schemaGen.generateSchema(EhrPatientRecord::class.java)
        val schemaString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
        println(schemaString)
    }
}