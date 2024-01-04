package com.hartwig.actin.clinical.feed

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.io.File
import java.io.IOException

class FeedFileReader<T : FeedEntry>(feedClass: Class<T>) {
    private val reader = CsvMapper().apply {
        enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        enable(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS)
        enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
    }.readerFor(feedClass).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

    @Throws(IOException::class)
    fun read(feedTsv: String): List<T> {
        return reader.readValues<T>(File(feedTsv)).readAll()
    }
}