package com.hartwig.actin.clinical.serialization

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.util.Paths.forceTrailingFileSeparator
import com.hartwig.actin.util.json.GsonSerializer.create
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

object ClinicalRecordJson {
    private const val CLINICAL_JSON_EXTENSION = ".clinical.json"

    @Throws(IOException::class)
    fun write(records: List<ClinicalRecord>, directory: String) {
        val path = forceTrailingFileSeparator(directory)
        for (record in records) {
            val jsonFile = path + record.patientId() + CLINICAL_JSON_EXTENSION
            val writer = BufferedWriter(FileWriter(jsonFile))
            writer.write(toJson(record))
            writer.close()
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readFromDir(directory: String): List<ClinicalRecord> {
        val records: MutableList<ClinicalRecord> = Lists.newArrayList()
        val files = File(directory).listFiles()
            ?: throw IllegalArgumentException("Could not retrieve clinical json files from $directory")
        for (file in files) {
            if (file.getName().endsWith(CLINICAL_JSON_EXTENSION)) {
                records.add(fromJson(Files.readString(file.toPath())))
            }
        }
        records.sort(ClinicalRecordComparator())
        return records
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(clinicalJson: String): ClinicalRecord {
        return fromJson(Files.readString(File(clinicalJson).toPath()))
    }

    @JvmStatic
    @VisibleForTesting
    fun toJson(record: ClinicalRecord): String {
        return create().toJson(record)
    }

    @JvmStatic
    @VisibleForTesting
    fun fromJson(json: String): ClinicalRecord {
        return ClinicalGsonDeserializer.create().fromJson(json, ImmutableClinicalRecord::class.java)
    }
}
