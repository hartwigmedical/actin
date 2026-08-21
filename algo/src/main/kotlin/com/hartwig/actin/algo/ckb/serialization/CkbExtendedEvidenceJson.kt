package com.hartwig.actin.algo.ckb.serialization

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceEntry
import com.hartwig.actin.util.json.ActinObjectMapper
import java.io.File
import java.nio.file.Files

object CkbExtendedEvidenceJson {

    private val mapper: ObjectMapper by lazy { ActinObjectMapper.create() }

    fun read(ckbExtendedEvidenceJson: String): List<CkbExtendedEvidenceEntry> {
        return fromJson(Files.readString(File(ckbExtendedEvidenceJson).toPath()))
    }

    fun fromJson(json: String): List<CkbExtendedEvidenceEntry> {
        return mapper.readValue(json, object : TypeReference<List<CkbExtendedEvidenceEntry>>() {})
    }

    fun toJson(entries: List<CkbExtendedEvidenceEntry>): String {
        return mapper.writeValueAsString(entries)
    }
}
