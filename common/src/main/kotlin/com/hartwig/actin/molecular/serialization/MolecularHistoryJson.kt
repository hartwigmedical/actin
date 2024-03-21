package com.hartwig.actin.molecular.serialization

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonLocalDateAdapter
import com.hartwig.actin.util.json.GsonSerializer
import com.hartwig.actin.util.json.MolecularHistoryAdapter
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.time.LocalDate

object MolecularHistoryJson {
    private val LOGGER = LogManager.getLogger(MolecularHistoryJson::class.java)
    private const val MOLECULAR_HISTORY_JSON_EXTENSION = ".molecular_history.json"

    fun write(molecularHistory: MolecularHistory, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + molecularHistory.patientId + MOLECULAR_HISTORY_JSON_EXTENSION
        LOGGER.info("Writing molecular history to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(molecularHistory))
        writer.close()
    }

    fun read(molecularHistoryJson: String): MolecularHistory {
        return fromJson(Files.readString(File(molecularHistoryJson).toPath()))
    }

    fun toJson(molecularHistory: MolecularHistory): String {
        return GsonSerializer.create().toJson(molecularHistory)
    }

    fun fromJson(json: String): MolecularHistory {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(object : TypeToken<LocalDate?>() {}.type, GsonLocalDateAdapter())
            .registerTypeAdapter(MolecularHistory::class.java, MolecularHistoryAdapter(gsonBuilder.create()))
            .create()
            .fromJson(json, MolecularHistory::class.java)
    }
}