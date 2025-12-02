package com.hartwig.actin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.serialization.ComorbidityAdapter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.util.json.GsonLocalDateAdapter
import com.hartwig.actin.util.json.GsonSerializer
import com.hartwig.actin.util.json.TreatmentAdapter
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

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
        return gson().toJson(patientRecord)
    }

    fun fromJson(json: String): PatientRecord {
        return gson().fromJson(json, PatientRecord::class.java)
    }

    private fun gson(): Gson {
        val gsonBuilder = GsonSerializer.createBuilder()
        return gsonBuilder
            .registerTypeAdapter(object : TypeToken<LocalDate?>() {}.type, GsonLocalDateAdapter())
            .registerTypeAdapter(Treatment::class.java, TreatmentAdapter(gsonBuilder.create()))
            .registerTypeAdapter(Comorbidity::class.java, ComorbidityAdapter())
            .create()
    }
}