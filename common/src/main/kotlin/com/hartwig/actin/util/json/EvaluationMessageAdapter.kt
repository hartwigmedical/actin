package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.StaticMessage

class EvaluationMessageAdapter(private val gson: Gson) : TypeAdapter<EvaluationMessage>() {

    override fun write(out: JsonWriter, value: EvaluationMessage?) {
        if (value == null) {
            out.nullValue()
            return
        }

        val jsonObject = gson.toJsonTree(StaticMessage(value.toString())).asJsonObject
        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): EvaluationMessage? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        return gson.fromJson(jsonObject, StaticMessage::class.java)
    }
}