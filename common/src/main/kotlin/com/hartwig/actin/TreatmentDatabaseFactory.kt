package com.hartwig.actin

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.serialization.ClinicalGsonDeserializer
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path

object TreatmentDatabaseFactory {

    private const val DRUG_JSON = "drug.json"
    private const val TREATMENT_JSON = "treatment.json"
    private val LOGGER = LogManager.getLogger(TreatmentDatabaseFactory::class.java)

    fun createFromPath(treatmentDbPath: String): TreatmentDatabase {
        LOGGER.info("Creating treatment database from path {}", treatmentDbPath)
        val drugsByName = drugJsonToMapByName(readFile(treatmentDbPath, DRUG_JSON))
        val treatments = treatmentsFromFile(drugsByName, readFile(treatmentDbPath, TREATMENT_JSON))

        val treatmentsByName = treatments.flatMap { treatment ->
            (treatment.synonyms + treatment.name).map { it.replace(" ", "_").lowercase() to treatment }
        }.toMap()
        
        LOGGER.info(
            " Loaded {} drugs from {} and {} treatments from {}",
            drugsByName.size,
            DRUG_JSON,
            treatments.size,
            TREATMENT_JSON
        )
        return TreatmentDatabase(drugsByName, treatmentsByName)
    }

    private fun drugJsonToMapByName(drugJson: String): Map<String, Drug> {
        val drugs: List<Drug> = ClinicalGsonDeserializer.create().fromJson(drugJson, object : TypeToken<List<Drug>>() {}.type)
        return drugs.associateBy { it.name.lowercase() }
    }

    private fun treatmentsFromFile(drugsByName: Map<String, Drug>, treatmentJson: String): List<Treatment> {
        return ClinicalGsonDeserializer.createWithDrugMap(drugsByName)
            .fromJson(treatmentJson, object : TypeToken<List<Treatment>>() {}.type)
    }

    private fun readFile(basePath: String, filename: String): String {
        return Files.readString(Path.of(basePath, filename))
    }
}
