package com.hartwig.actin

import com.hartwig.actin.clinical.serialization.ClinicalRecordJsonMapper
import com.hartwig.actin.datamodel.PatientRecord
import java.nio.file.Files
import java.nio.file.Path
import org.apache.logging.log4j.LogManager

object PatientRecordJson {
    private val logger = LogManager.getLogger(PatientRecordJson::class.java)
    private const val PATIENT_RECORD_JSON_EXTENSION = ".patient_record.json"

    fun write(patientRecord: PatientRecord, directory: String) {
        write(patientRecord, Path.of(directory))
    }

    fun write(patientRecord: PatientRecord, directory: Path) {
        val jsonFile = directory.resolve(patientRecord.patientId + PATIENT_RECORD_JSON_EXTENSION)
        logger.info("Writing patient record to {}", jsonFile)
        val writer = Files.newBufferedWriter(jsonFile)
        writer.write(toJson(patientRecord))
        writer.close()
    }

    fun read(patientRecordJson: String): PatientRecord {
        return read(Path.of(patientRecordJson))
    }

    fun read(patientRecordJson: Path): PatientRecord {
        return fromJson(Files.readString(patientRecordJson))
    }

    fun toJson(patientRecord: PatientRecord): String {
        return ClinicalRecordJsonMapper.create().toJson(patientRecord)
    }

    fun fromJson(json: String): PatientRecord {
        return ClinicalRecordJsonMapper.create().fromJson(json, PatientRecord::class.java)
    }

}