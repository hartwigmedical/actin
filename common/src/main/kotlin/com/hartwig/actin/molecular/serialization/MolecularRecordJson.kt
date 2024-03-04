package com.hartwig.actin.molecular.serialization

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonLocalDateAdapter
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.time.LocalDate

object MolecularRecordJson {

    private val LOGGER = LogManager.getLogger(MolecularRecordJson::class.java)
    private const val MOLECULAR_JSON_EXTENSION = ".molecular.json"

    fun write(record: MolecularRecord, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + record.sampleId + MOLECULAR_JSON_EXTENSION
        LOGGER.info("Writing molecular record to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(record))
        writer.close()
    }

    fun read(molecularJson: String): MolecularRecord {
        return fromJson(Files.readString(File(molecularJson).toPath()))
    }

    fun toJson(record: MolecularRecord): String {
        return GsonSerializer.create().toJson(record)
    }

    fun fromJson(json: String): MolecularRecord {
        return GsonBuilder().serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(object : TypeToken<LocalDate?>() {}.type, GsonLocalDateAdapter())
            .create()
            .fromJson(json, MolecularRecord::class.java)
    }
}
