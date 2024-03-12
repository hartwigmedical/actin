package com.hartwig.actin.clinical.serialization

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.util.Paths.forceTrailingFileSeparator
import com.hartwig.actin.util.json.GsonSerializer.create
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files

object ClinicalRecordJson {

    private const val CLINICAL_JSON_EXTENSION = ".clinical.json"

    fun write(records: List<ClinicalRecord>, directory: String) {
        val path = forceTrailingFileSeparator(directory)
        for (record in records) {
            val jsonFile = path + record.patientId + CLINICAL_JSON_EXTENSION
            val writer = BufferedWriter(FileWriter(jsonFile))
            writer.write(toJson(record))
            writer.close()
        }
    }

    fun readFromDir(directory: String): List<ClinicalRecord> {
        val files = File(directory).listFiles()
            ?: throw IllegalArgumentException("Could not retrieve clinical json files from $directory")

        return files.filter { it.getName().endsWith(CLINICAL_JSON_EXTENSION) }
            .map { fromJson(Files.readString(it.toPath())) }
            .sortedWith(ClinicalRecordComparator())
    }

    fun read(clinicalJson: String): ClinicalRecord {
        return fromJson(Files.readString(File(clinicalJson).toPath()))
    }

    fun toJson(record: ClinicalRecord): String {
        return create().toJson(record)
    }

    fun fromJson(json: String): ClinicalRecord {
        return ClinicalGsonDeserializer.create().fromJson(json, ClinicalRecord::class.java)
    }
}
