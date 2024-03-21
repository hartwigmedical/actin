package com.hartwig.actin

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularHistoryAdapter
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonLocalDateAdapter
import com.hartwig.actin.util.json.GsonLocalDateTimeAdapter
import com.hartwig.actin.util.json.GsonSerializer
import com.hartwig.actin.util.json.TreatmentAdapter
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime

object PatientRecordJson {
    private val LOGGER = LogManager.getLogger(PatientRecordJson::class.java)
    private const val PATIENT_RECORD_JSON_EXTENSION = ".patient_record.json"

    fun write(patientRecord: PatientRecord, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + patientRecord.patientId + PATIENT_RECORD_JSON_EXTENSION
        LOGGER.info("Writing patient record to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(patientRecord))
        writer.close()
    }

    fun read(patientRecordJson: String): PatientRecord {
        return fromJson(Files.readString(File(patientRecordJson).toPath()))
    }

    fun toJson(patientRecord: PatientRecord): String {
        return GsonSerializer.create().toJson(patientRecord)
    }

    fun fromJson(json: String): PatientRecord {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(object : TypeToken<LocalDate?>() {}.type, GsonLocalDateAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeAdapter())
            .registerTypeAdapter(MolecularHistory::class.java, MolecularHistoryAdapter(gsonBuilder.create()))
            .registerTypeAdapter(Treatment::class.java, TreatmentAdapter(gsonBuilder.create()))
            .create()
            .fromJson(json, PatientRecord::class.java)
    }
}