package com.hartwig.actin.algo.ckb.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.algo.ckb.json.JsonCkbExtendedEvidenceEntry
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files

object CkbExtendedEvidenceJson {

    fun read(ckbExtendedEvidenceJson: String): List<JsonCkbExtendedEvidenceEntry> {
        return fromJson(Files.readString(File(ckbExtendedEvidenceJson).toPath()))
    }

    fun fromJson(json: String): List<JsonCkbExtendedEvidenceEntry> {
        val returnType: Type = object : TypeToken<List<JsonCkbExtendedEvidenceEntry>>() {}.type

        return createGson().fromJson(json, returnType)
    }

    fun createGson(): Gson {
        return GsonBuilder().serializeNulls().create()
    }
}