package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.ObservedToxicity
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.json.GsonSerializer
import java.util.Objects

class TreatmentHistoryEntryConfigFactory(
    private val treatmentDatabase: TreatmentDatabase
) : CurationConfigFactory<TreatmentHistoryEntryConfig> {

    private val gson = GsonSerializer.create()

    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<TreatmentHistoryEntryConfig> {
        val input = parts[fields["input"]!!]
        val treatmentName = fields["treatmentName"]?.let { ResourceFile.optionalString(parts[it]) } ?: ""
        val ignore: Boolean = CurationUtil.isIgnoreString(treatmentName)
        val (treatmentHistoryEntry, validationErrors) = if (ignore) null to emptyList() else curateObject(
            input,
            fields,
            parts.toList(),
            treatmentName
        )

        return ValidatedCurationConfig(
            TreatmentHistoryEntryConfig(
                input = input,
                ignore = ignore,
                curated = treatmentHistoryEntry
            ), validationErrors
        )
    }

    private fun curateObject(
        input: String,
        fields: Map<String, Int>,
        parts: List<String>,
        treatmentName: String
    ): Pair<TreatmentHistoryEntry?, List<CurationConfigValidationError>> {
        val isTrial = optionalObjectFromColumn(parts, fields, "isTrial", ResourceFile::optionalBool) ?: false

        val treatments = if (treatmentName.isEmpty() && isTrial) emptySet() else {
            val treatmentsByName = CurationUtil.toSet(treatmentName).associateWith(treatmentDatabase::findTreatmentByName)
            val unknownTreatmentNames = treatmentsByName.filterValues(Objects::isNull).keys
            if (unknownTreatmentNames.isNotEmpty()) {
                return null to missingTreatmentException(unknownTreatmentNames, input, "treatmentName")
            }
            treatmentsByName.values.filterNotNull().toSet()
        }

        val bestResponseString = optionalStringFromColumn(parts, fields, "bestResponse")
        val bestResponse =
            if (bestResponseString != null) TreatmentResponse.createFromString(bestResponseString) else null
        val stopReasonDetail = optionalStringFromColumn(parts, fields, "stopReason")

        val toxicities: Set<ObservedToxicity>? = stopReasonDetail?.let {
            if (it.lowercase().contains("toxicity")) {
                setOf(ObservedToxicity(name = it, categories = emptySet(), grade = null))
            } else emptySet()
        }

        val bodyLocationCategories = entriesFromColumn(parts, fields, "bodyLocationCategories")
            ?.map { stringToEnum(it, BodyLocationCategory::valueOf) }?.toSet()

        val intents = entriesFromColumn(parts, fields, "intents")?.map { stringToEnum(it, Intent::valueOf) }?.toSet()

        val (maintenanceTreatmentStage, maintenanceValidationErrors) = treatmentStage(
            input,
            parts,
            fields,
            "maintenanceTreatment",
            "maintenanceTreatmentStartYear",
            "maintenanceTreatmentStartMonth",
            null
        )

        val (switchToTreatments, switchToValidationErrors) = treatmentStage(
            input,
            parts,
            fields,
            "switchToTreatment",
            "switchToTreatmentStartYear",
            "switchToTreatmentStartMonth",
            "switchToTreatmentCycles"
        )

        val treatmentHistoryDetails = TreatmentHistoryDetails(
            stopYear = optionalIntegerFromColumn(parts, fields, "stopYear"),
            stopMonth = optionalIntegerFromColumn(parts, fields, "stopMonth"),
            cycles = optionalIntegerFromColumn(parts, fields, "cycles"),
            bestResponse = bestResponse,
            stopReasonDetail = stopReasonDetail,
            stopReason = if (stopReasonDetail != null) StopReason.createFromString(stopReasonDetail) else null,
            toxicities = toxicities,
            bodyLocationCategories = bodyLocationCategories,
            bodyLocations = entriesFromColumn(parts, fields, "bodyLocations"),
            maintenanceTreatment = maintenanceTreatmentStage,
            switchToTreatments = switchToTreatments?.let(::listOf),
            ongoingAsOf = null
        )

        return TreatmentHistoryEntry(
            treatments = treatments,
            startYear = optionalIntegerFromColumn(parts, fields, "startYear"),
            startMonth = optionalIntegerFromColumn(parts, fields, "startMonth"),
            intents = intents,
            isTrial = isTrial,
            trialAcronym = optionalStringFromColumn(parts, fields, "trialAcronym"),
            treatmentHistoryDetails = treatmentHistoryDetails
        ) to switchToValidationErrors + maintenanceValidationErrors
    }

    private fun treatmentStage(
        input: String,
        parts: List<String>,
        fields: Map<String, Int>,
        nameField: String,
        startYearField: String,
        startMonthField: String,
        cycleField: String?
    ): Pair<TreatmentStage?, List<CurationConfigValidationError>> {
        return optionalStringFromColumn(parts, fields, nameField)?.let { name ->
            val treatment = treatmentDatabase.findTreatmentByName(name)
                ?: return null to missingTreatmentException(setOf(name), input, nameField)

            TreatmentStage(
                treatment = treatment,
                startYear = optionalIntegerFromColumn(parts, fields, startYearField),
                startMonth = optionalIntegerFromColumn(parts, fields, startMonthField),
                cycles = cycleField?.let { optionalIntegerFromColumn(parts, fields, it) }
            ) to emptyList()
        } ?: (null to emptyList())
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

    private fun missingTreatmentException(treatments: Set<String>, input: String, nameField: String): List<CurationConfigValidationError> {
        return treatments.map { it.replace(" ", "_").uppercase() }.map {
            CurationConfigValidationError(
                CurationCategory.ONCOLOGICAL_HISTORY,
                input,
                nameField,
                it,
                "treatment",
                templates(it)
            )
        }
    }

    private fun templates(it: String) =
        "Treatment with name $it does not exist in database. Please add with one of the following templates: " + listOf(
            DrugTreatment(name = it, synonyms = emptySet(), isSystemic = false, drugs = emptySet()),
            Radiotherapy(name = it, synonyms = emptySet(), isSystemic = false),
            OtherTreatment(name = it, synonyms = emptySet(), isSystemic = false, categories = emptySet())
        ).map { templates ->
            gson.toJson(templates).replace("isSystemic\":false", "isSystemic\":?")
                .replace("\"displayOverride\":null,", "")
                .replace("\"maxCycles\":null,", "")
        }
}