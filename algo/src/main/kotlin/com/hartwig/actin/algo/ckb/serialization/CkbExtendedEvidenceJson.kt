package com.hartwig.actin.algo.ckb.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files

object CkbExtendedEvidenceJson {

    fun read(ckbExtendedEvidenceJson: String): List<CkbExtendedEvidenceEntry> {
        return fromJson(Files.readString(File(ckbExtendedEvidenceJson).toPath()))
    }

    fun fromJson(json: String): List<CkbExtendedEvidenceEntry> {
        val returnType: Type = object : TypeToken<List<CkbExtendedEvidenceEntry>>() {}.type

        return createGson().fromJson(json, returnType)
    }

    fun createGson(): Gson {
        return GsonBuilder().serializeNulls().setFieldNamingStrategy(MixedFieldNamingStrategy()).create()
    }
}