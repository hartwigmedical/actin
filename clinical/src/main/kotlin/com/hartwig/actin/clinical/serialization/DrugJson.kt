package com.hartwig.actin.clinical.serialization

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object DrugJson {
    @Throws(IOException::class)
    fun read(file: String): List<Drug> {
        val contents = Files.readString(Path.of(file))
        return ClinicalGsonDeserializer.create().fromJson(contents, object : TypeToken<List<Drug>>() {}.type)
    }
}