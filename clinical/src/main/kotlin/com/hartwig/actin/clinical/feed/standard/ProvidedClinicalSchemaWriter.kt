package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.Description
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.victools.jsonschema.generator.FieldScope
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class ProvidedClinicalSchemaWriter {

    fun write() {
        val mapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }
        val configBuilder = SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
        configBuilder.forFields().withRequiredCheck { convert(it)?.isMarkedNullable == false }
            .withNullableCheck { convert(it)?.isMarkedNullable == true }
            .withDescriptionResolver {
                getKProperty(it)?.findAnnotation<Description>()?.value ?: "none"
            }
        val config: SchemaGeneratorConfig = configBuilder.withObjectMapper(mapper).build()
        val generator = SchemaGenerator(config)
        val jsonSchema: JsonNode = generator.generateSchema(ProvidedPatientRecord::class.java)
        mapper.writeValue(
            File("${System.getProperty("user.dir")}/clinical/src/main/resources/json_schema/provided_clinical_data.schema.json"),
            jsonSchema
        )
    }
}

fun convert(fieldScope: FieldScope): KType? {
    return getKProperty(fieldScope)?.returnType
}

private fun getKProperty(fieldScope: FieldScope): KProperty1<out Any, *>? {
    val target = fieldScope.declarationDetails.schemaTargetType
    val memberProperties = target.erasedType.kotlin.memberProperties
    return memberProperties.find { it.name == fieldScope.name }
}

fun main() {
    ProvidedClinicalSchemaWriter().write()
}