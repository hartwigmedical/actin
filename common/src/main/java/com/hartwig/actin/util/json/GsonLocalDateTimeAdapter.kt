package com.hartwig.actin.util.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GsonLocalDateTimeAdapter : TypeAdapter<LocalDateTime?>() {

    override fun write(writer: JsonWriter, localDateTime: LocalDateTime?) {
        if (localDateTime == null) {
            writer.nullValue()
        } else {
            writer.value(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        val firstToken = reader.peek()
        if (firstToken == JsonToken.NULL) {
            return null
        }
        val dateTimeString = reader.nextString()
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
    }
}
