package com.hartwig.actin

import com.hartwig.actin.clinical.serialization.ClinicalRecordJsonMapper
import com.hartwig.actin.datamodel.PatientRecord
import java.nio.file.Files
import java.nio.file.Path
import io.github.oshai.kotlinlogging.KotlinLogging

object PatientRecordJson {
    private val logger = KotlinLogging.logger {}
    private const val PATIENT_RECORD_JSON_EXTENSION = ".patient_record.json"

    fun write(patientRecord: PatientRecord, directory: String) {
        write(patientRecord, Path.of(directory))
    }

    fun write(patientRecord: PatientRecord, directory: Path) {
        val jsonFile = directory.resolve(patientRecord.patientId + PATIENT_RECORD_JSON_EXTENSION)
        logger.info { "Writing patient record to $jsonFile" }
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

    private val mapper by lazy { ClinicalRecordJsonMapper.create() }

    fun toJson(patientRecord: PatientRecord): String {
        return mapper.writeValueAsString(patientRecord)
    }

    fun fromJson(json: String): PatientRecord {
        return mapper.readValue(json, PatientRecord::class.java)
    }
}
