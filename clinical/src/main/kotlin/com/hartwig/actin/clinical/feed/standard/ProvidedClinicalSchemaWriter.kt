package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator
import java.io.File

class ProvidedClinicalSchemaWriter {

    fun write() {
        val mapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }
        val schema =
            JsonSchemaGenerator(mapper, JsonSchemaConfig.nullableJsonSchemaDraft4().withFailOnUnknownProperties(false)).generateJsonSchema(
                ProvidedPatientRecord::class.java
            )

        mapper.writeValue(
            File("${System.getProperty("user.dir")}/clinical/src/main/resources/json_schema/provided_clinical_data.schema.json"),
            schema
        )
    }
}

fun main() {
    ProvidedClinicalSchemaWriter().write()
}