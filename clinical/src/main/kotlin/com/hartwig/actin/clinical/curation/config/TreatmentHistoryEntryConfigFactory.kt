package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.json.GsonSerializer
import java.util.*

class TreatmentHistoryEntryConfigFactory(
    private val treatmentDatabase: TreatmentDatabase
) : CurationConfigFactory<TreatmentHistoryEntryConfig> {

    private val gson = GsonSerializer.create()

    override fun create(fields: Map<String, Int>, parts: Array<String>): TreatmentHistoryEntryConfig {
        val treatmentName = fields["treatmentName"]?.let { ResourceFile.optionalString(parts[it]) } ?: ""
        val ignore: Boolean = CurationUtil.isIgnoreString(treatmentName)
        val treatmentHistoryEntry = if (ignore) null else curateObject(fields, parts.toList(), treatmentName)

        return TreatmentHistoryEntryConfig(
            input = parts[fields["input"]!!],
            ignore = ignore,
            curated = treatmentHistoryEntry
        )
    }

    private fun curateObject(fields: Map<String, Int>, parts: List<String>, treatmentName: String): TreatmentHistoryEntry {
        val isTrial = optionalObjectFromColumn(parts, fields, "isTrial", ResourceFile::optionalBool) ?: false

        val treatments = if (treatmentName.isEmpty() && isTrial) emptyList() else {
            val treatmentsByName = CurationUtil.toSet(treatmentName).associateWith(treatmentDatabase::findTreatmentByName)
            val unknownTreatmentNames = treatmentsByName.filterValues(Objects::isNull).keys
            if (unknownTreatmentNames.isNotEmpty()) {
                throw missingTreatmentException(unknownTreatmentNames)
            }
            treatmentsByName.values
        }

        val bestResponseString = optionalStringFromColumn(parts, fields, "bestResponse")
        val bestResponse =
            if (bestResponseString != null) TreatmentResponse.createFromString(bestResponseString) else null
        val stopReasonDetail = optionalStringFromColumn(parts, fields, "stopReason")

        val toxicities: Set<ObservedToxicity>? = stopReasonDetail?.let {
            if (it.lowercase().contains("toxicity")) {
                setOf(ImmutableObservedToxicity.builder().name(it).categories(emptySet()).build())
            } else emptySet()
        }

        val bodyLocationCategories = entriesFromColumn(parts, fields, "bodyLocationCategories")
            ?.map { stringToEnum(it, BodyLocationCategory::valueOf) }

        val treatmentHistoryDetails = ImmutableTreatmentHistoryDetails.builder()
            .stopYear(optionalIntegerFromColumn(parts, fields, "stopYear"))
            .stopMonth(optionalIntegerFromColumn(parts, fields, "stopMonth"))
            .cycles(optionalIntegerFromColumn(parts, fields, "cycles"))
            .bestResponse(bestResponse)
            .stopReasonDetail(stopReasonDetail)
            .stopReason(if (stopReasonDetail != null) StopReason.createFromString(stopReasonDetail) else null)
            .toxicities(toxicities)
            .bodyLocationCategories(bodyLocationCategories)
            .bodyLocations(entriesFromColumn(parts, fields, "bodyLocations"))
            .build()

        val intents = entriesFromColumn(parts, fields, "intents")?.map { stringToEnum(it, Intent::valueOf) }

        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(treatments)
            .startYear(optionalIntegerFromColumn(parts, fields, "startYear"))
            .startMonth(optionalIntegerFromColumn(parts, fields, "startMonth"))
            .intents(intents)
            .isTrial(isTrial)
            .trialAcronym(optionalStringFromColumn(parts, fields, "trialAcronym"))
            .treatmentHistoryDetails(treatmentHistoryDetails)
            .build()
    }

    private fun optionalIntegerFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): Int? {
        return optionalObjectFromColumn(parts, fields, colName, ResourceFile::optionalInteger)
    }

    private fun optionalStringFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): String? {
        return optionalObjectFromColumn(parts, fields, colName, ResourceFile::optionalString)
    }

    private fun <T> optionalObjectFromColumn(
        parts: List<String>,
        fields: Map<String, Int>,
        colName: String,
        extractObject: (String) -> T?
    ): T? {
        return fields[colName]?.let { extractObject(parts[it]) }
    }

    private fun entriesFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): Set<String>? {
        return optionalStringFromColumn(parts, fields, colName)?.let(CurationUtil::toSet)
    }

    private fun <T> stringToEnum(input: String, enumCreator: (String) -> T): T {
        return enumCreator(input.uppercase().replace(" ", "_"))
    }

    private fun missingTreatmentException(treatments: Set<String>): IllegalStateException {
        return IllegalStateException(treatments.map { it.replace(" ", "_").uppercase() }.map {
            "Treatment with name $it does not exist in database. Please add with one of the following templates: \n" + listOf(
                ImmutableDrugTreatment.builder().name(it).synonyms(emptySet()).isSystemic(false).drugs(emptySet())
                    .build(),
                ImmutableRadiotherapy.builder().name(it).synonyms(emptySet()).isSystemic(false).build(),
                ImmutableOtherTreatment.builder().name(it).synonyms(emptySet()).isSystemic(false)
                    .categories(emptySet())
                    .build()
            ).map { templates ->
                gson.toJson(templates).replace("isSystemic\":false", "isSystemic\":?")
                    .replace("\"displayOverride\":null,", "")
                    .replace("\"maxCycles\":null,", "")
            }
        }.joinToString(","))
    }
}