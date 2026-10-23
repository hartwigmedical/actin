package com.hartwig.actin.util.json

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonInclude.Value
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

object ActinObjectMapper {

    fun create(): ObjectMapper = ObjectMapper()
        .registerModule(
            KotlinModule.Builder()
                .enable(KotlinFeature.NullIsSameAsDefault)
                .build()
        )
        .registerModule(JavaTimeModule())
        .registerModule(actinDefaultsModule())
        .setDefaultPropertyInclusion(Value.construct(Include.ALWAYS, Include.ALWAYS))
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
        .setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), true)
        .configure(JsonWriteFeature.WRITE_NAN_AS_STRINGS.mappedFeature(), false)

    private fun actinDefaultsModule(): SimpleModule {
        return SimpleModule("ActinDefaultsModule").apply {
            addSerializer(Set::class.java, SortedSetSerializer)
            addAbstractTypeMapping(Set::class.java, LinkedHashSet::class.java)
        }
    }
}
