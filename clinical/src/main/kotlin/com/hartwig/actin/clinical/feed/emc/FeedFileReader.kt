package com.hartwig.actin.clinical.feed.emc

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
import org.apache.logging.log4j.LogManager
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

class FeedTemporalDeserializer<T : Temporal>(private val parser: (String, DateTimeFormatter) -> T?) : JsonDeserializer<T?>() {
    companion object {
        private const val BASE_FORMAT = "yyyy-MM-dd HH:mm:ss."
        private val MILLIS_FORMATTER = DateTimeFormatter.ofPattern("${BASE_FORMAT}SSS")
        const val NANOS_FORMAT = "${BASE_FORMAT}SSSSSSS"
        private val NANOS_FORMATTER = DateTimeFormatter.ofPattern(NANOS_FORMAT)
    }

    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): T? {
        val date = jsonParser.text
        return if ("NULL".equals(date, ignoreCase = true) || date.trim().isEmpty()) {
            null
        } else {
            if (date.length == NANOS_FORMAT.length) {
                parser.invoke(date, NANOS_FORMATTER)
            } else {
                parser.invoke(date, MILLIS_FORMATTER)
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
        val cleanInput = p.text.trim()
        return if (cleanInput == "NULL") {
            ""
        } else {
            cleanInput
        }
    }
}

class FeedSubjectDeserializer : JsonDeserializer<String>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
        return p.text.replace("-".toRegex(), "")
    }
}

data class FeedResult<T : FeedEntry>(
    val entry: T, val validation: FeedValidation
)

class FeedFileReader<T : FeedEntry>(
    feedClass: Class<T>,
    private val feedValidator: FeedValidator<T> = AlwaysValidFeedValidator(),
    private val feedFilePreprocessor: FeedFilePreprocessor = FeedFilePreprocessor(),
    private val clinicalFeedCreator: (List<FeedResult<T>>) -> EmcClinicalFeed
) {
    private val reader = CsvMapper().apply {
        enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        enable(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS)
        enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
        registerModule(
            SimpleModule().addDeserializer(
                LocalDate::class.java,
                FeedTemporalDeserializer { text, format -> LocalDate.parse(text, format) })
                .addDeserializer(LocalDateTime::class.java, FeedTemporalDeserializer { text, format -> LocalDateTime.parse(text, format) })
                .addDeserializer(String::class.java, FeedStringDeserializer())
        )
    }.readerFor(feedClass).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

    fun read(feedTsv: String): EmcClinicalFeed {
        LOGGER.info(" Reading {}", feedTsv)
        val preProcessedFile = feedFilePreprocessor.apply(File(feedTsv))
        val results = reader.readValues<T>(preProcessedFile).readAll().map {
            FeedResult(it, feedValidator.validate(it))
        }
        LOGGER.info(" Read {} entries from {}", results.size, feedTsv)
        preProcessedFile.delete()
        return clinicalFeedCreator.invoke(results.filter { it.validation.valid })
            .copy(validationWarnings = results.flatMap { it.validation.warnings })
    }

    companion object {
        private val LOGGER = LogManager.getLogger(FeedFileReader::class.java)
    }
}