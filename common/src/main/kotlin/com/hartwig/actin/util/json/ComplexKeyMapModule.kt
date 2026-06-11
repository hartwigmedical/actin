package com.hartwig.actin.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.type.MapType

/**
 * Replacement of Gson's enableComplexMapKeySerialization()
 */
class ComplexKeyMapModule : Module() {

    override fun getModuleName(): String = "ComplexKeyMapModule"

    override fun version(): Version = Version.unknownVersion()

    override fun setupModule(context: SetupContext) {
        context.addSerializers(ComplexKeyMapSerializers)
        context.addDeserializers(ComplexKeyMapDeserializers)
    }

    private object ComplexKeyMapSerializers : Serializers.Base() {

        override fun findMapSerializer(
            config: SerializationConfig,
            type: MapType,
            beanDesc: BeanDescription,
            keySerializer: JsonSerializer<Any>?,
            elementTypeSerializer: TypeSerializer?,
            elementValueSerializer: JsonSerializer<Any>?
        ): JsonSerializer<*>? {
            return if (isComplexKey(type.keyType)) ComplexKeyMapSerializer else null
        }
    }

    private object ComplexKeyMapSerializer : JsonSerializer<Map<Any?, Any?>>() {

        override fun serialize(value: Map<Any?, Any?>, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeStartArray()
            value.forEach { (k, v) ->
                gen.writeStartArray()
                if (k == null) gen.writeNull() else provider.defaultSerializeValue(k, gen)
                if (v == null) gen.writeNull() else provider.defaultSerializeValue(v, gen)
                gen.writeEndArray()
            }
            gen.writeEndArray()
        }
    }

    private object ComplexKeyMapDeserializers : Deserializers.Base() {

        override fun findMapDeserializer(
            type: MapType,
            config: DeserializationConfig,
            beanDesc: BeanDescription,
            keyDeserializer: KeyDeserializer?,
            elementTypeDeserializer: TypeDeserializer?,
            elementDeserializer: JsonDeserializer<*>?
        ): JsonDeserializer<*>? {
            return if (isComplexKey(type.keyType)) ComplexKeyMapDeserializer(type) else null
        }
    }

    private class ComplexKeyMapDeserializer(private val mapType: MapType) : JsonDeserializer<Map<Any?, Any?>>() {

        override fun deserialize(parser: JsonParser, context: DeserializationContext): Map<Any?, Any?> {
            val keyType = mapType.keyType
            val valueType = mapType.contentType
            val tree = parser.codec.readTree<JsonNode>(parser)
            val result = linkedMapOf<Any?, Any?>()
            if (tree.isObject && tree.isEmpty) return result
            if (!tree.isArray) {
                throw JsonMappingException.from(parser, "Expected array of [key, value] pairs for map with complex key, got: $tree")
            }
            tree.forEach { entry ->
                if (!entry.isArray || entry.size() != 2) {
                    throw JsonMappingException.from(parser, "Expected 2-element [key, value] array for complex key map entry, got: $entry")
                }
                val key: Any? = context.readTreeAsValue(entry.get(0), keyType)
                val value: Any? = context.readTreeAsValue(entry.get(1), valueType)
                result[key] = value
            }
            return result
        }
    }

    companion object {

        private fun isComplexKey(keyType: JavaType): Boolean {
            val raw = keyType.rawClass
            return !(raw == String::class.java
                    || raw.isPrimitive
                    || Number::class.java.isAssignableFrom(raw)
                    || raw == java.lang.Boolean::class.java
                    || raw == Character::class.java
                    || raw.isEnum)
        }
    }
}
