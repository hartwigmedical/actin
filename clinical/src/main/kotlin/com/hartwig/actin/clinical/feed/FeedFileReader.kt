package com.hartwig.actin.clinical.feed

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FeedDateDeserializer : JsonDeserializer<LocalDate?>() {
    companion object {
        private const val BASE_FORMAT = "yyyy-MM-dd HH:mm:ss."
        private val MILLIS_FORMATTER = DateTimeFormatter.ofPattern("${BASE_FORMAT}SSS")
        const val NANOS_FORMAT = "${BASE_FORMAT}SSSSSSS"
        private val NANOS_FORMATTER = DateTimeFormatter.ofPattern(NANOS_FORMAT)
    }

    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): LocalDate? {
        val date = jsonParser.text
        return if ("NULL".equals(date, ignoreCase = true)) {
            null
        } else {
            if (date.length == NANOS_FORMAT.length) {
                LocalDate.parse(date, NANOS_FORMATTER)
            } else {
                LocalDate.parse(date, MILLIS_FORMATTER)
            }
        }
    }
}

class EuropeanDecimalDeserializer : JsonDeserializer<Double?>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double? {
        return p.text.replace("\\.", "").replace(',', '.').toDoubleOrNull()
    }
}

class FeedStringDeserializer : JsonDeserializer<String>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
        val cleanInput = p.text.replace("\n", "").replace("\r", "")
        return if (cleanInput == "NULL") {
            ""
        } else {
            cleanInput
        }
    }
}

class FeedFileReader<T : FeedEntry>(feedClass: Class<T>, private val feedValidator: FeedValidator<T> = AlwaysValidFeedValidator()) {
    private val reader = CsvMapper().apply {
        enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        enable(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
        registerModule(
            SimpleModule().addDeserializer(LocalDate::class.java, FeedDateDeserializer()).addDeserializer(
                String::class.java, FeedStringDeserializer()
            )
        )
    }.readerFor(feedClass).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

    fun read(feedTsv: String): List<T> {
        return reader.readValues<T>(File(feedTsv)).readAll().filter { feedValidator.validate(it) }
    }
}