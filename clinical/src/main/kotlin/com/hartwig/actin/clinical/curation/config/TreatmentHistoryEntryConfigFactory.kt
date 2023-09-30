package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager

object TreatmentHistoryEntryConfigFactory {
    private val LOGGER = LogManager.getLogger(TreatmentHistoryEntryConfigFactory::class.java)
    private val gson = GsonSerializer.create()

    fun createConfig(
        treatmentName: String,
        treatmentDatabase: TreatmentDatabase,
        parts: List<String>,
        fields: Map<String, Int>
    ): TreatmentHistoryEntryConfig? {
        val ignore: Boolean = CurationUtil.isIgnoreString(treatmentName)
        val treatmentHistoryEntry = if (ignore) {
            null
        } else {
            val treatment = treatmentDatabase.findTreatmentByName(treatmentName)
            curateObject(fields, parts, treatment, treatmentName)
        }

        return if (ignore || treatmentHistoryEntry != null) {
            TreatmentHistoryEntryConfig(
                input = parts[fields["input"]!!],
                ignore = ignore,
                curated = treatmentHistoryEntry
            )
        } else {
            null
        }
    }

    private fun logMissingTreatmentMessage(treatmentName: String) {
        val formattedTreatmentName = treatmentName.replace(" ", "_").uppercase()
        LOGGER.warn("  Treatment with name $formattedTreatmentName does not exist in database. Please add with one of the following templates:")

        listOf(
            ImmutableDrugTherapy.builder().name(formattedTreatmentName).synonyms(emptySet()).isSystemic(false).drugs(emptySet()).build(),
            ImmutableRadiotherapy.builder().name(formattedTreatmentName).synonyms(emptySet()).isSystemic(false).build(),
            ImmutableOtherTreatment.builder().name(formattedTreatmentName).synonyms(emptySet()).isSystemic(false).categories(emptySet())
                .build()
        ).forEach {
            val treatmentProposal = gson.toJson(it).replace("isSystemic\":false", "isSystemic\":?")
                .replace("\"displayOverride\":null,", "")
                .replace("\"maxCycles\":null,", "")
            LOGGER.warn("    $treatmentProposal,")
        }
    }

    private fun curateObject(
        fields: Map<String, Int>,
        parts: List<String>,
        treatment: Treatment?,
        treatmentName: String,
    ): TreatmentHistoryEntry? {

        val isTrial = optionalObjectFromColumn(parts, fields, "isTrial", ResourceFile::optionalBool) ?: false

        if (treatment == null && treatmentName.isNotEmpty() && !isTrial) {
            logMissingTreatmentMessage(treatmentName)
            return null
        }

        val therapyHistoryDetails = if (treatment is Therapy) {
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

            ImmutableTherapyHistoryDetails.builder()
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
        } else null

        val intents = entriesFromColumn(parts, fields, "intents")?.map { stringToEnum(it, Intent::valueOf) }

        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(setOfNotNull(treatment))
            .startYear(optionalIntegerFromColumn(parts, fields, "startYear"))
            .startMonth(optionalIntegerFromColumn(parts, fields, "startMonth"))
            .intents(intents)
            .isTrial(isTrial)
            .trialAcronym(optionalStringFromColumn(parts, fields, "trialAcronym"))
            .therapyHistoryDetails(therapyHistoryDetails)
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
}