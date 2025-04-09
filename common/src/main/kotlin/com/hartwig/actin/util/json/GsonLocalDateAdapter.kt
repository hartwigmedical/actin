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
            writer.value(localDate.toString())
        }
    }

    override fun read(reader: JsonReader): LocalDate? {
        val firstToken: JsonToken = reader.peek()
        when (firstToken) {
            JsonToken.NULL -> {
                reader.skipValue()
                return null
            }

            JsonToken.STRING -> {
                reader.peek()
                return LocalDate.parse(reader.nextString())
            }

            else -> {
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
    }
}
