package com.hartwig.actin.util.json

import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.StaticMessage

class EvaluationMessageAdapter() : TypeAdapter<EvaluationMessage>() {

    override fun write(out: JsonWriter, value: EvaluationMessage?) {
        if (value == null) {
            out.nullValue()
            return
        }
        
        val staticMsg = StaticMessage(value.toString())
        out.beginObject()
        out.name("message").value(staticMsg.message)
        out.endObject()
    }

    override fun read(input: JsonReader): EvaluationMessage? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val message = jsonObject.get("message").asString
        return StaticMessage(message)
    }
}