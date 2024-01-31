package com.hartwig.actin.clinical.nki

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator
import org.junit.Test


class EhrPatientRecordTest {

    @Test
    fun `Create schema`() {
        val mapper = ObjectMapper()
        val schemaGen = JsonSchemaGenerator(mapper)
        val schema: JsonSchema = schemaGen.generateSchema(EhrPatientRecord::class.java)
        val schemaString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
        println(schemaString)
    }
}