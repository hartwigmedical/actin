package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.serialization.MolecularTestAdapter

class MolecularHistoryAdapter(private val gson: Gson) : TypeAdapter<MolecularHistory>() {

    override fun write(out: JsonWriter, value: MolecularHistory) {
        val jsonObject = gson.toJsonTree(value).asJsonObject
        val molecularTestsArray = JsonArray()
        value.molecularTests.forEach { molecularTest ->
            molecularTestsArray.add(MolecularTestAdapter(gson).toJsonTree(molecularTest))
        }
        jsonObject.add("molecularTests", molecularTestsArray)

        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): MolecularHistory {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val molecularTestsJsonArray = jsonObject.getAsJsonArray("molecularTests")
        val molecularTests = molecularTestsJsonArray.map { element ->
            MolecularTestAdapter(gson).fromJsonTree(element)
        }

        jsonObject.remove("molecularTests")
        return MolecularHistory(molecularTests)
    }
}