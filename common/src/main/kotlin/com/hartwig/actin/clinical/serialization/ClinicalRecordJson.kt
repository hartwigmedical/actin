package com.hartwig.actin.clinical.serialization

import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import java.nio.file.Files
import java.nio.file.Path

object ClinicalRecordJson {

    private const val CLINICAL_JSON_EXTENSION = ".clinical.json"

    fun write(records: List<ClinicalRecord>, directory: Path) {
        for (record in records) {
            val jsonFile = directory.resolve(record.patientId + CLINICAL_JSON_EXTENSION)
            val writer = Files.newBufferedWriter(jsonFile)
            writer.write(toJson(record))
            writer.close()
        }
    }

    fun readFromDir(directory: String): List<ClinicalRecord> {
        return readFromDir(Path.of(directory))
    }

    fun readFromDir(directory: Path): List<ClinicalRecord> {
        Files.isDirectory(directory) || throw IllegalArgumentException("Not a directory: $directory")
        return Files.list(directory).filter { it.fileName.toString().endsWith(CLINICAL_JSON_EXTENSION) }
            .map { read(it) }
            .toList()
            .sortedWith(ClinicalRecordComparator())
    }

    fun read(clinicalJson: String): ClinicalRecord {
        return read(Path.of(clinicalJson))
    }

    fun read(clinicalJson: Path): ClinicalRecord {
        return fromJson(Files.readString(clinicalJson))
    }

    fun toJson(record: ClinicalRecord): String {
        return ClinicalGsonDeserializer.create().toJson(record)
    }

    fun fromJson(json: String): ClinicalRecord {
        return ClinicalGsonDeserializer.create().fromJson(json, ClinicalRecord::class.java)
    }
}
