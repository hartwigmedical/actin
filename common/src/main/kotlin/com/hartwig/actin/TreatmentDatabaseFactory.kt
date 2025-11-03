package com.hartwig.actin

import com.google.gson.Gson
import com.hartwig.actin.clinical.serialization.ClinicalGsonDeserializer
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

object TreatmentDatabaseFactory {

    private const val DRUG_FOLDER = "drugs"
    private const val TREATMENT_FOLDER = "treatments"
    private val LOGGER = LogManager.getLogger(TreatmentDatabaseFactory::class.java)

    fun createFromPath(treatmentDbPath: String): TreatmentDatabase {
        val drugsByName = readFilesInFolder(
            Path.of(treatmentDbPath, DRUG_FOLDER),
            ClinicalGsonDeserializer.create(),
            Drug::class.java
        ).associateBy { it.name.lowercase() }

        val treatmentsByName = readFilesInFolder(
            Path.of(treatmentDbPath, TREATMENT_FOLDER),
            ClinicalGsonDeserializer.createWithDrugMap(drugsByName),
            Treatment::class.java
        ).flatMap { treatment ->
            (treatment.synonyms + treatment.name).map { it.replace(" ", "_").lowercase() to treatment }
        }.toMap()

        LOGGER.info(
            " Loaded {} drugs from {} and {} treatments from {}",
            drugsByName.size,
            DRUG_FOLDER,
            treatmentsByName.size,
            TREATMENT_FOLDER
        )
        return TreatmentDatabase(drugsByName, treatmentsByName)
    }

    fun<T> readFilesInFolder(path: Path, des: Gson, classOfT: Class<T>): List<T> {
        return Files.walk(path).use { stream ->
            stream.filter { it.isRegularFile() }.iterator().asSequence()
                .map { path ->
                    Files.newBufferedReader(path).use { reader ->
                        des.fromJson(reader, classOfT)
                    }
                }.toList()
        }
    }
}
