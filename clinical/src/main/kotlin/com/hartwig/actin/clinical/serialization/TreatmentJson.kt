package com.hartwig.actin.clinical.serialization

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object TreatmentJson {
    @Throws(IOException::class)
    fun read(file: String, drugsByName: Map<String, Drug>): List<Treatment> {
        val contents = Files.readString(Path.of(file))
        return ClinicalGsonDeserializer.createWithDrugMap(drugsByName).fromJson(contents, object : TypeToken<List<Treatment>>() {}.type)
    }
}