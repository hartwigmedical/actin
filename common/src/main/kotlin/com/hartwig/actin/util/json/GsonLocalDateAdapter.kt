package com.hartwig.actin.util.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate

class GsonLocalDateAdapter : TypeAdapter<LocalDate?>() {

    override fun write(writer: JsonWriter, localDate: LocalDate?) {
        if (localDate == null) {
            writer.nullValue()
        } else {
            writer.beginObject()
            writer.name("year")
            writer.value(localDate.year.toLong())
            writer.name("month")
            writer.value(localDate.monthValue.toLong())
            writer.name("day")
            writer.value(localDate.dayOfMonth.toLong())
            writer.endObject()
        }
    }

    override fun read(reader: JsonReader): LocalDate? {
        val firstToken: JsonToken = reader.peek()
        if (firstToken == JsonToken.NULL) {
            reader.skipValue()
            return null
        }
        var year: Int = -1
        var month: Int = -1
        var day: Int = -1
        reader.beginObject()
        var field: String = reader.nextName()
        while (reader.hasNext()) {
            val token: JsonToken = reader.peek()
            if (token == JsonToken.NAME) {
                field = reader.nextName()
            }
            if ((field == "year")) {
                reader.peek()
                year = reader.nextInt()
            }
            if ((field == "month")) {
                reader.peek()
                month = reader.nextInt()
            }
            if ((field == "day")) {
                reader.peek()
                day = reader.nextInt()
            }
        }
        reader.endObject()
        return LocalDate.of(year, month, day)
    }
}
