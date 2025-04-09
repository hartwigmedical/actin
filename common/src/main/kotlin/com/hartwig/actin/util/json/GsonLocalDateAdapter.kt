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
        if (firstToken == JsonToken.NULL) {
            reader.skipValue()
            return null
        }
        reader.peek()
        return LocalDate.parse(reader.nextString())
    }
}
