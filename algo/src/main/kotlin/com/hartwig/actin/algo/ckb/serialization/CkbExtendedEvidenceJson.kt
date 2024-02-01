package com.hartwig.actin.algo.ckb.serialization

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import com.hartwig.actin.util.json.GsonLocalDateAdapter
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files
import java.time.LocalDate

object CkbExtendedEvidenceJson {

    fun read(ckbExtendedEvidenceJson: String): List<CkbExtendedEvidenceEntry> {
        val json = Files.readString(File(ckbExtendedEvidenceJson).toPath())
        val listType: Type = object : TypeToken<List<CkbExtendedEvidenceEntry>>() {}.type

        return GsonBuilder()
            .setFieldNamingStrategy(MixedFieldNamingStrategy())
            .registerTypeAdapter(object : TypeToken<LocalDate?>() {}.type, GsonLocalDateAdapter())
            .create()
            .fromJson(json, listType)
    }
}