package com.hartwig.actin

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.serialization.ClinicalGsonDeserializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

object TreatmentDatabaseFactory {
    private val DRUG_JSON: String = "drug.json"
    private val TREATMENT_JSON: String = "treatment.json"
    private val LOGGER: Logger = LogManager.getLogger(TreatmentDatabaseFactory::class.java)

    @JvmStatic
    @Throws(IOException::class)
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
        val drugs: List<Drug> = ClinicalGsonDeserializer.create().fromJson(drugJson, object : TypeToken<List<Drug?>?>() {}.getType())
        return drugs.stream().collect(
            Collectors.toMap(
                Function({ drug: Drug -> drug.name().lowercase(Locale.getDefault()) }), Function.identity()
            )
        )
    }

    private fun treatmentJsonToMapByName(treatmentJson: String, drugsByName: Map<String, Drug>): Map<String, Treatment> {
        val treatments: List<Treatment> = ClinicalGsonDeserializer.createWithDrugMap(drugsByName)
            .fromJson(treatmentJson, object : TypeToken<List<Treatment?>?>() {}.getType())
        return treatments.stream()
            .flatMap<Map.Entry<String, Treatment>>(Function<Treatment, Stream<out Map.Entry<String, Treatment>>>({ treatment: Treatment ->
                Stream.concat<String>(treatment.synonyms().stream(), Stream.of<String>(treatment.name()))
                    .map<Map.Entry<String, Treatment>>(Function<String, Map.Entry<String, Treatment>>({ name: String ->
                        java.util.Map.entry<String, Treatment>(
                            name.replace(" ", "_").lowercase(Locale.getDefault()),
                            treatment
                        )
                    }))
            }))
            .collect(
                Collectors.toMap<Map.Entry<String, Treatment>, String, Treatment>(
                    Function<Map.Entry<String, Treatment>, String>({ java.util.Map.Entry.key }),
                    Function<Map.Entry<String, Treatment>, Treatment>({ java.util.Map.Entry.value })
                )
            )
    }

    @Throws(IOException::class)
    private fun readFile(basePath: String, filename: String): String {
        return Files.readString(Path.of(basePath, filename))
    }
}
