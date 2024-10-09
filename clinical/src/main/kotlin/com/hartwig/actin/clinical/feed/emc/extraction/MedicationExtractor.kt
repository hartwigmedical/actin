package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import com.hartwig.actin.clinical.curation.CypInteractionCurationUtil
import com.hartwig.actin.clinical.curation.QTProlongatingCurationUtil
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.medication.MedicationEntry
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import org.apache.logging.log4j.LogManager

class MedicationExtractor(
    private val medicationNameCuration: CurationDatabase<MedicationNameConfig>,
    private val medicationDosageCuration: CurationDatabase<MedicationDosageConfig>,
    private val periodBetweenUnitCuration: CurationDatabase<PeriodBetweenUnitConfig>,
    private val cypInteractionCuration: CurationDatabase<CypInteractionConfig>,
    private val qtProlongatingCuration: CurationDatabase<QTProlongatingConfig>,
    private val administrationRouteTranslation: TranslationDatabase<String>,
    private val dosageUnitTranslation: TranslationDatabase<String>,
    private val atcModel: AtcModel
) {

    fun extract(patientId: String, entries: List<MedicationEntry>): ExtractionResult<List<Medication>> {
        return entries.map { entry ->
            val nameCuration = curateName(entry, patientId)
            val name = nameCuration.extracted
            if (name.isNullOrEmpty()) ExtractionResult(emptyList(), nameCuration.evaluation) else {
                val administrationRouteCuration = translateAdministrationRoute(patientId, entry.dosageInstructionRouteDisplay)
                val dosage = curateDosage(administrationRouteCuration.extracted, entry, patientId)

                val atc = atcModel.resolveByCode(entry.code5ATCCode, entry.code5ATCDisplay)
                val isSelfCare = entry.code5ATCDisplay.isEmpty() && entry.code5ATCCode.isEmpty()
                val isTrialMedication =
                    entry.code5ATCDisplay.isEmpty() && entry.code5ATCCode.isNotEmpty() && entry.code5ATCCode[0].lowercaseChar() !in 'a'..'z'
                if (atc == null && !isSelfCare && !isTrialMedication) {
                    LOGGER.warn("Medication $name has no ATC code and is not self-care or a trial")
                }

                val medication = Medication(
                    dosage = dosage.extracted,
                    name = name,
                    status = curateMedicationStatus(patientId, entry.status),
                    administrationRoute = administrationRouteCuration.extracted,
                    startDate = entry.periodOfUseValuePeriodStart,
                    stopDate = entry.periodOfUseValuePeriodEnd,
                    cypInteractions = CypInteractionCurationUtil.curateMedicationCypInteractions(cypInteractionCuration, name),
                    qtProlongatingRisk = QTProlongatingCurationUtil.annotateWithQTProlongating(qtProlongatingCuration, name),
                    atc = atc,
                    isSelfCare = isSelfCare,
                    isTrialMedication = isTrialMedication
                )

                val evaluation = listOf(nameCuration, administrationRouteCuration, dosage)
                    .fold(CurationExtractionEvaluation()) { acc, result -> acc + result.evaluation }
                ExtractionResult(listOf(medication), evaluation)
            }
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }

    private fun curateName(entry: MedicationEntry, patientId: String): ExtractionResult<String?> {
        val atcName = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay)
        return if (atcName.isNotEmpty()) {
            ExtractionResult(atcName, CurationExtractionEvaluation())
        } else {
            val input = fullTrim(entry.codeText)
            val curation = CurationResponse.createFromConfigs(
                medicationNameCuration.find(input),
                patientId,
                CurationCategory.MEDICATION_NAME,
                input,
                "medication name",
                true
            )
            ExtractionResult(curation.config()?.let { if (!it.ignore) it.name else null }, curation.extractionEvaluation)
        }
    }

    fun translateAdministrationRoute(patientId: String, administrationRoute: String?): ExtractionResult<String?> =
        translateString(
            patientId, administrationRoute, administrationRouteTranslation::find,
            CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION, "medication administration route"
        )

    private fun curateDosage(
        administrationRoute: String?, entry: MedicationEntry, patientId: String
    ): ExtractionResult<Dosage> {
        return if (dosageRequiresCuration(administrationRoute, entry)) {
            val input = entry.dosageInstructionText.trim { it <= ' ' }
            val curationResponse = CurationResponse.createFromConfigs(
                medicationDosageCuration.find(input),
                patientId,
                CurationCategory.MEDICATION_DOSAGE,
                input,
                "medication dosage",
                true
            )
            val dosage = curationResponse.config()?.curated ?: Dosage()

            ExtractionResult(dosage, curationResponse.extractionEvaluation)
        } else {
            val dosageUnitTranslation = translateDosageUnit(patientId, entry.dosageInstructionDoseQuantityUnit)
            val periodBetweenUnitCuration = curatePeriodBetweenUnit(patientId, entry.dosageInstructionPeriodBetweenDosagesUnit)
            ExtractionResult(
                Dosage(
                    dosageMin = entry.dosageInstructionDoseQuantityValue,
                    dosageMax = correctDosageMax(entry),
                    dosageUnit = dosageUnitTranslation.extracted,
                    frequency = entry.dosageInstructionFrequencyValue,
                    frequencyUnit = entry.dosageInstructionFrequencyUnit,
                    periodBetweenValue = entry.dosageInstructionPeriodBetweenDosagesValue,
                    periodBetweenUnit = periodBetweenUnitCuration.extracted,
                    ifNeeded = extractIfNeeded(entry)
                ),
                dosageUnitTranslation.evaluation + periodBetweenUnitCuration.evaluation
            )
        }
    }

    fun translateDosageUnit(patientId: String, dosageUnit: String?): ExtractionResult<String?> =
        translateString(
            patientId, dosageUnit?.lowercase(), dosageUnitTranslation::find,
            CurationCategory.DOSAGE_UNIT_TRANSLATION, "medication dosage unit"
        )

    fun curatePeriodBetweenUnit(patientId: String, input: String?): ExtractionResult<String?> {
        return if (input.isNullOrEmpty()) {
            ExtractionResult(null, CurationExtractionEvaluation())
        } else {
            val curation = CurationResponse.createFromConfigs(
                periodBetweenUnitCuration.find(input),
                patientId,
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION,
                input,
                "period between unit",
                true
            )
            ExtractionResult(curation.config()?.interpretation, curation.extractionEvaluation)
        }
    }

    fun curateMedicationStatus(patientId: String, status: String): MedicationStatus? =
        when (status.lowercase()) {
            "" -> {
                null
            }

            "active" -> {
                MedicationStatus.ACTIVE
            }

            "on-hold" -> {
                MedicationStatus.ON_HOLD
            }

            "kuur geannuleerd" -> {
                MedicationStatus.CANCELLED
            }

            else -> {
                LOGGER.warn("Could not interpret medication status: $status for patient $patientId")
                MedicationStatus.UNKNOWN
            }
        }

    private fun extractIfNeeded(entry: MedicationEntry) =
        entry.dosageInstructionAsNeededDisplay.trim().lowercase() == "zo nodig"

    private fun correctDosageMax(entry: MedicationEntry): Double? {
        return if (entry.dosageInstructionMaxDosePerAdministration == 0.0) {
            entry.dosageInstructionDoseQuantityValue
        } else {
            entry.dosageInstructionMaxDosePerAdministration
        }
    }

    private fun dosageRequiresCuration(administrationRoute: String?, entry: MedicationEntry) =
        administrationRoute?.lowercase() == "oral" && (entry.dosageInstructionDoseQuantityValue == 0.0 ||
                entry.dosageInstructionDoseQuantityUnit.isEmpty() ||
                entry.dosageInstructionFrequencyValue == 0.0 ||
                entry.dosageInstructionFrequencyUnit.isEmpty())

    private fun translateString(
        patientId: String,
        input: String?,
        translate: (String) -> Translation<String>?,
        curationCategory: CurationCategory,
        translationType: String
    ): ExtractionResult<String?> {
        return if (input.isNullOrEmpty()) {
            ExtractionResult(null, CurationExtractionEvaluation())
        } else {
            val curationResponse = CurationResponse.createFromTranslation(
                translate.invoke(input), patientId, curationCategory, input, translationType
            )
            ExtractionResult(curationResponse.config()?.translated?.ifEmpty { null }, curationResponse.extractionEvaluation)
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(MedicationExtractor::class.java)
        fun create(
            curationDatabaseContext: CurationDatabaseContext,
            atcModel: AtcModel
        ) =
            MedicationExtractor(
                medicationNameCuration = curationDatabaseContext.medicationNameCuration,
                medicationDosageCuration = curationDatabaseContext.medicationDosageCuration,
                periodBetweenUnitCuration = curationDatabaseContext.periodBetweenUnitCuration,
                cypInteractionCuration = curationDatabaseContext.cypInteractionCuration,
                qtProlongatingCuration = curationDatabaseContext.qtProlongingCuration,
                administrationRouteTranslation = curationDatabaseContext.administrationRouteTranslation,
                dosageUnitTranslation = curationDatabaseContext.dosageUnitTranslation,
                atcModel = atcModel
            )
    }
}