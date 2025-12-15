package com.hartwig.actin

import com.google.gson.Gson
import com.hartwig.actin.clinical.serialization.ClinicalGsonSerializer
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Path

object TreatmentDatabaseFactory {

    private const val DRUG_FOLDER = "drugs"
    private const val TREATMENT_FOLDER = "treatments"
    private val LOGGER = LogManager.getLogger(TreatmentDatabaseFactory::class.java)

    fun createFromPath(treatmentDbPath: String): TreatmentDatabase {
        val drugsByName = readFilesInFolder<Drug>(treatmentDbPath, DRUG_FOLDER, ClinicalGsonSerializer.create())
            .associateBy { it.name.lowercase() }

        val treatmentsByName =
            readFilesInFolder<Treatment>(treatmentDbPath, TREATMENT_FOLDER, ClinicalGsonSerializer.createWithDrugMap(drugsByName))
                .flatMap { treatment ->
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

    @Suppress("unused")
    fun writeToPath(treatmentDbPath: String, treatmentDatabase: TreatmentDatabase) {
        val drugsByName = treatmentDatabase.getDrugsByName()
        writeFilesInFolder(treatmentDbPath, DRUG_FOLDER, drugsByName, ClinicalGsonSerializer.create())
        writeFilesInFolder(treatmentDbPath, TREATMENT_FOLDER, treatmentDatabase.getTreatmentsByName(), ClinicalGsonSerializer.createWithDrugMap(drugsByName))
    }

    private inline fun <reified T> writeFilesInFolder(treatmentDbPath: String, folderName: String, content: Map<String, T>, serializer: Gson) {
        val folder = getPath(treatmentDbPath, folderName)
        folder.listFiles()?.forEach { it.deleteRecursively() }
        content.forEach { (fileName, value) ->
            File(folder, "$fileName.json").writeText(serializer.toJson(value))
        }
    }

    private inline fun <reified T> readFilesInFolder(treatmentDbPath: String, folderName: String, deserializer: Gson): List<T> {
        val folder = getPath(treatmentDbPath, folderName)
        return folder.walkTopDown()
            .filter { it.isFile }
            .map { file ->
                file.bufferedReader().use { reader ->
                    deserializer.fromJson(reader, T::class.java)
                }
            }
            .toList()
    }

    private fun getPath(treatmentDbPath: String, folderName: String): File {
        val folder = Path.of(treatmentDbPath, folderName).toFile()
        require(folder.exists() && folder.isDirectory) {
            throw NoSuchFileException("Folder does not exist or is not a directory ${folder.path}")
        }
        return folder
    }
}
