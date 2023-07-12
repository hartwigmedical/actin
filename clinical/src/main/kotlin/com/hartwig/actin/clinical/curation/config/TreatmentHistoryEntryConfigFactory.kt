package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver
import com.hartwig.actin.util.ResourceFile
import org.apache.logging.log4j.LogManager

object TreatmentHistoryEntryConfigFactory {
    private val LOGGER = LogManager.getLogger(TreatmentHistoryEntryConfigFactory::class.java)

    fun createConfig(
        treatmentName: String,
        treatmentDatabase: TreatmentDatabase,
        parts: List<String>,
        fields: Map<String, Int>
    ): TreatmentHistoryEntryConfig {
        val ignore: Boolean = CurationUtil.isIgnoreString(treatmentName)
        val treatment = if (ignore) null else {
            treatmentDatabase.findTreatmentByName(treatmentName) ?: generateTreatmentForCuration(treatmentName, parts, fields)
        }
        return TreatmentHistoryEntryConfig(
            input = parts[fields["input"]!!],
            ignore = ignore,
            curated = if (!ignore) curateObject(fields, parts, treatment) else null
        )
    }

    private fun generateTreatmentForCuration(treatmentName: String, parts: List<String>, fields: Map<String, Int>): Treatment? {
        val categories = TreatmentCategoryResolver.fromStringList(parts[fields["category"]!!]).filterNot { it == TreatmentCategory.TRIAL }
        val therapyCategories = categories.filter {
            it in setOf(
                TreatmentCategory.CHEMOTHERAPY,
                TreatmentCategory.HORMONE_THERAPY,
                TreatmentCategory.IMMUNOTHERAPY,
                TreatmentCategory.TARGETED_THERAPY
            )
        }
        val isSystemic = ResourceFile.optionalBool(parts[fields["isSystemic"]!!])
            ?: if (categories.contains(TreatmentCategory.SURGERY)) false else null
        if (therapyCategories.isNotEmpty()) {
            LOGGER.warn(
                "  Treatment with name $treatmentName does not exist in database and has therapy categories ({})",
                therapyCategories.joinToString(", ")
            )
        } else if (isSystemic == null) {
            LOGGER.warn("  Treatment with name $treatmentName does not exist in database and it is unknown whether it is systemic")
        } else {
            val treatment = ImmutableOtherTreatment.builder()
                .name(treatmentName)
                .addAllCategories(categories)
                .synonyms(emptySet())
                .isSystemic(isSystemic)
                .build()
            LOGGER.info("  Automatically generated treatment from curation data: $treatment")
            return treatment
        }
        return null
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
            .treatments(treatment?.let { setOf(treatment) } ?: emptySet())
            .startYear(optionalIntegerFromColumn(parts, fields, "startYear"))
            .startMonth(optionalIntegerFromColumn(parts, fields, "startMonth"))
            .intents(intents)
            .isTrial(TreatmentCategoryResolver.fromStringList(parts[fields["category"]!!]).contains(TreatmentCategory.TRIAL))
            .trialAcronym(optionalStringFromColumn(parts, fields, "trialAcronym"))
            .therapyHistoryDetails(therapyHistoryDetails)
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