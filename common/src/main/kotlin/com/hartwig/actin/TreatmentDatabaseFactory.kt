package com.hartwig.actin

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.serialization.ClinicalGsonDeserializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Path

object TreatmentDatabaseFactory {

    private const val DRUG_JSON: String = "drug.json"
    private const val TREATMENT_JSON: String = "treatment.json"
    private val LOGGER: Logger = LogManager.getLogger(TreatmentDatabaseFactory::class.java)

    fun createFromPath(treatmentDbPath: String): TreatmentDatabase {
        LOGGER.info("Creating treatment database from path {}", treatmentDbPath)
        val drugsByName: Map<String, Drug> = drugJsonToMapByName(readFile(treatmentDbPath, DRUG_JSON))
        val treatmentsByName: Map<String, Treatment> = treatmentJsonToMapByName(readFile(treatmentDbPath, TREATMENT_JSON), drugsByName)
        LOGGER.info(
            " Loaded {} drugs from {} and {} treatments from {}",
            drugsByName.size,
            DRUG_JSON,
            treatmentsByName.size,
            TREATMENT_JSON
        )
        return TreatmentDatabase(drugsByName, treatmentsByName)
    }

    private fun drugJsonToMapByName(drugJson: String): Map<String, Drug> {
        val drugs: List<Drug> = ClinicalGsonDeserializer.create().fromJson(drugJson, object : TypeToken<List<Drug>>() {}.type)
        return drugs.associateBy { it.name.lowercase() }
    }

    private fun treatmentJsonToMapByName(treatmentJson: String, drugsByName: Map<String, Drug>): Map<String, Treatment> {
        val deserializer = ClinicalGsonDeserializer.createWithDrugMap(drugsByName)
        val treatments: List<Treatment> = deserializer.fromJson(treatmentJson, object : TypeToken<List<Treatment>>() {}.type)

        return treatments.flatMap { treatment: Treatment ->
            (treatment.synonyms + treatment.name).map { it.replace(" ", "_").lowercase() to treatment }
        }.toMap()
    }

    private fun readFile(basePath: String, filename: String): String {
        return Files.readString(Path.of(basePath, filename))
    }
}
