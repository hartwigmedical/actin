package com.hartwig.actin.algo.ckb.serialization

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files

object CkbExtendedEvidenceJson {

    fun read(ckbExtendedEvidenceJson: String): List<CkbExtendedEvidenceEntry> {
        val json = Files.readString(File(ckbExtendedEvidenceJson).toPath())
        val returnType: Type = object : TypeToken<List<CkbExtendedEvidenceEntry>>() {}.type

        return GsonBuilder().setFieldNamingStrategy(MixedFieldNamingStrategy()).create().fromJson(json, returnType)
    }
}