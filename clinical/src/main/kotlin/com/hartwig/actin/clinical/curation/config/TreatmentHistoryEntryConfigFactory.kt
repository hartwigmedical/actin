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
import org.apache.logging.log4j.LogManager


object TreatmentHistoryEntryConfigFactory {
    private val LOGGER = LogManager.getLogger(TreatmentHistoryEntryConfigFactory::class.java)

    fun createConfig(
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
            val bestResponseString = optionalStringFromColumn(parts, fields, "bestResponse")
            val bestResponse = if (bestResponseString != null) TreatmentResponse.createFromString(bestResponseString) else null
            val stopReasonDetail = optionalStringFromColumn(parts, fields, "stopReason")

            val toxicities: Set<ObservedToxicity>? = stopReasonDetail?.let {
                if (it.lowercase().contains("toxicity")) {
                    setOf(ImmutableObservedToxicity.builder().name(it).categories(emptySet()).build())
                } else emptySet()
            }

            ImmutableTherapyHistoryDetails.builder()
                .stopYear(optionalIntegerFromColumn(parts, fields, "stopYear"))
                .stopMonth(optionalIntegerFromColumn(parts, fields, "stopMonth"))
                .cycles(optionalIntegerFromColumn(parts, fields, "cycles"))
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
            .startYear(optionalIntegerFromColumn(parts, fields, "startYear"))
            .startMonth(optionalIntegerFromColumn(parts, fields, "startMonth"))
            .intents(intents)
            .isTrial(treatment?.categories()?.contains(TreatmentCategory.TRIAL))
            .trialAcronym(optionalStringFromColumn(parts, fields, "trialAcronym"))
            .therapyHistoryDetails(therapyHistoryDetails)
            .bodyLocationCategories(bodyLocationCategories)
            .bodyLocations(entriesFromColumn(parts, fields, "bodyLocations"))
            .build()
    }

    private fun optionalIntegerFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): Int? {
        return fields[colName]?.let { ResourceFile.optionalInteger(parts[it]) }
    }

    private fun optionalStringFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): String? {
        return fields[colName]?.let { ResourceFile.optionalString(parts[it]) }
    }

    private fun entriesFromColumn(parts: List<String>, fields: Map<String, Int>, colName: String): Set<String>? {
        return optionalStringFromColumn(parts, fields, colName)?.let(CurationUtil::toSet)
    }

    private fun <T> stringToEnum(input: String, enumCreator: (String) -> T): T {
        return enumCreator(input.uppercase().replace(" ", "_"))
    }
}