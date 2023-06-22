package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Files


object TreatmentHistoryEntryConfigFactory {
    private val LOGGER = LogManager.getLogger(TreatmentHistoryEntryConfigFactory::class.java)

    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun read(tsv: String, treatmentsByName: Map<String, Treatment>): List<TreatmentHistoryEntryConfig> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
        val (configs, searchedNames) = lines.drop(1)
            .flatMap { line ->
                val parts = line.split(DELIMITER)
                entriesFromColumn(parts, fields, "treatmentName")?.map { Pair(it.lowercase(), parts) } ?: emptyList()
            }
            .map { (treatmentName, parts) ->
                createConfig(treatmentName, treatmentsByName, parts, fields)
            }
            .reduce { acc, pair -> Pair(acc.first + pair.first, acc.second + pair.second) }

        (treatmentsByName.keys - searchedNames).forEach { LOGGER.warn("Treatment with name '$it' not used in resolving prior treatments") }
        return configs
    }

    private fun createConfig(
        treatmentName: String,
        treatmentsByName: Map<String, Treatment>,
        parts: List<String>,
        fields: Map<String, Int>
    ): Pair<List<TreatmentHistoryEntryConfig>, Set<String>> {
        val ignore: Boolean = CurationUtil.isIgnoreString(treatmentName)
        val treatment = treatmentsByName[treatmentName]
        if (!ignore && treatment == null) {
            LOGGER.warn("Could not find treatment with name $treatmentName")
        }
        val config = TreatmentHistoryEntryConfig(
            input = parts[fields["input"]!!],
            ignore = ignore,
            curated = if (!ignore) curateObject(fields, parts, treatment) else null
        )
        return Pair(listOf(config), setOf(treatmentName))
    }

    private fun curateObject(fields: Map<String, Int>, parts: List<String>, treatment: Treatment?): TreatmentHistoryEntry {
        val therapyHistoryDetails = if (treatment is Therapy) {
            val bestResponseString = ResourceFile.optionalString(parts[fields["bestResponse"]!!])
            val bestResponse = if (bestResponseString != null) TreatmentResponse.createFromString(bestResponseString) else null
            val stopReasonDetail = ResourceFile.optionalString(parts[fields["stopReason"]!!])

            val toxicities: Set<ObservedToxicity>? = stopReasonDetail?.let {
                if (it.lowercase().contains("toxicity")) {
                    setOf(ImmutableObservedToxicity.builder().name(it).categories(emptySet()).build())
                } else emptySet()
            }

            ImmutableTherapyHistoryDetails.builder()
                .stopYear(ResourceFile.optionalInteger(parts[fields["stopYear"]!!]))
                .stopMonth(ResourceFile.optionalInteger(parts[fields["stopMonth"]!!]))
                .cycles(ResourceFile.optionalInteger(parts[fields["cycles"]!!]))
                .bestResponse(bestResponse)
                .stopReasonDetail(stopReasonDetail)
                .stopReason(if (stopReasonDetail != null) StopReason.createFromString(stopReasonDetail) else null)
                .toxicities(toxicities)
                .build()
        } else null

        val intents = entriesFromColumn(parts, fields, "intents")?.map { stringToEnum(it, Intent::valueOf) }
        val bodyLocationCategories = entriesFromColumn(parts, fields, "bodyLocationCategories")
            ?.map { stringToEnum(it, BodyLocationCategory::valueOf) }

        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(treatment?.let { setOf(treatment) } ?: emptySet())
            .rawInput(parts[fields["input"]!!])
            .startYear(ResourceFile.optionalInteger(parts[fields["startYear"]!!]))
            .startMonth(ResourceFile.optionalInteger(parts[fields["startMonth"]!!]))
            .intents(intents)
            .isTrial(treatment?.categories()?.contains(TreatmentCategory.TRIAL))
            .trialAcronym(ResourceFile.optionalString(parts[fields["trialAcronym"]!!]))
            .therapyHistoryDetails(therapyHistoryDetails)
            .bodyLocationCategories(bodyLocationCategories)
            .bodyLocations(entriesFromColumn(parts, fields, "bodyLocations"))
            .build()
    }

    private fun entriesFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): List<String>? {
        return ResourceFile.optionalString(parts[fields[colName]!!])
            ?.split(";")
            ?.map { it.trim() }
            ?.filterNot { it.isEmpty() }
    }

    private fun <T> stringToEnum(input: String, enumCreator: (String) -> T): T {
        return enumCreator(input.uppercase().replace(" ", "_"))
    }
}