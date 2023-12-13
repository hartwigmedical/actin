package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationService
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import org.apache.logging.log4j.LogManager

class MedicationExtractor(
    private val medicationNameCuration: CurationDatabase<MedicationNameConfig>,
    private val medicationDosageCuration: CurationDatabase<MedicationDosageConfig>,
    private val periodBetweenUnitCuration: CurationDatabase<PeriodBetweenUnitConfig>,
    private val cypInterationCuration: CurationDatabase<CypInteractionConfig>,
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

                val atc = atcModel.resolve(entry.code5ATCCode)
                val isSelfCare = entry.code5ATCDisplay.isEmpty() && entry.code5ATCCode.isEmpty()
                val isTrialMedication =
                    entry.code5ATCDisplay.isEmpty() && entry.code5ATCCode.isNotEmpty() && entry.code5ATCCode[0].lowercaseChar() !in 'a'..'z'
                if (atc == null && !isSelfCare && !isTrialMedication) {
                    LOGGER.warn("Medication $name has no ATC code and is not self-care or a trial")
                }

                val medication = ImmutableMedication.builder()
                    .dosage(dosage.extracted)
                    .name(name)
                    .status(curateMedicationStatus(patientId, entry.status))
                    .administrationRoute(administrationRouteCuration.extracted)
                    .startDate(entry.periodOfUseValuePeriodStart)
                    .stopDate(entry.periodOfUseValuePeriodEnd)
                    .addAllCypInteractions(curateMedicationCypInteractions(name))
                    .qtProlongatingRisk(annotateWithQTProlongating(name))
                    .atc(atc)
                    .isSelfCare(isSelfCare)
                    .isTrialMedication(isTrialMedication)
                    .build()

                val evaluation = listOf(nameCuration, administrationRouteCuration, dosage)
                    .fold(ExtractionEvaluation()) { acc, result -> acc + result.evaluation }
                ExtractionResult(listOf(medication), evaluation)
            }
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }

    private fun curateName(entry: MedicationEntry, patientId: String): ExtractionResult<String?> {
        val atcName = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay)
        return if (atcName.isNotEmpty()) {
            ExtractionResult(atcName, ExtractionEvaluation())
        } else {
            val input = fullTrim(entry.codeText)
            val curation = CurationResponse.createFromConfigs(
                medicationNameCuration.curate(input),
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
            patientId, administrationRoute, administrationRouteTranslation::translate,
            CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION, "medication administration route"
        )

    private fun curateDosage(
        administrationRoute: String?, entry: MedicationEntry, patientId: String
    ): ExtractionResult<Dosage> {
        return if (dosageRequiresCuration(administrationRoute, entry)) {
            val input = entry.dosageInstructionText.trim { it <= ' ' }
            val curationResponse = CurationResponse.createFromConfigs(
                medicationDosageCuration.curate(input),
                patientId,
                CurationCategory.MEDICATION_DOSAGE,
                input,
                "medication dosage",
                true
            )
            val dosage = curationResponse.config()?.curated ?: ImmutableDosage.builder().build()

            ExtractionResult(dosage, curationResponse.extractionEvaluation)
        } else {
            val dosageUnitTranslation = translateDosageUnit(patientId, entry.dosageInstructionDoseQuantityUnit)
            val periodBetweenUnitCuration = curatePeriodBetweenUnit(patientId, entry.dosageInstructionPeriodBetweenDosagesUnit)
            ExtractionResult(
                ImmutableDosage.builder()
                    .dosageMin(entry.dosageInstructionDoseQuantityValue)
                    .dosageMax(correctDosageMax(entry))
                    .dosageUnit(dosageUnitTranslation.extracted)
                    .frequency(entry.dosageInstructionFrequencyValue)
                    .frequencyUnit(entry.dosageInstructionFrequencyUnit)
                    .periodBetweenValue(entry.dosageInstructionPeriodBetweenDosagesValue)
                    .periodBetweenUnit(periodBetweenUnitCuration.extracted)
                    .ifNeeded(extractIfNeeded(entry))
                    .build(),
                dosageUnitTranslation.evaluation + periodBetweenUnitCuration.evaluation
            )
        }
    }

    fun translateDosageUnit(patientId: String, dosageUnit: String?): ExtractionResult<String?> =
        translateString(
            patientId, dosageUnit?.lowercase(), dosageUnitTranslation::translate,
            CurationCategory.DOSAGE_UNIT_TRANSLATION, "medication dosage unit"
        )

    fun curatePeriodBetweenUnit(patientId: String, input: String?): ExtractionResult<String?> {
        return if (input.isNullOrEmpty()) {
            ExtractionResult(null, ExtractionEvaluation())
        } else {
            val curation = CurationResponse.createFromConfigs(
                periodBetweenUnitCuration.curate(input),
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

    private fun curateMedicationCypInteractions(medicationName: String): List<CypInteraction> {
        return this.cypInterationCuration.curate(medicationName).map { it }.flatMap(CypInteractionConfig::interactions)
    }

    private fun annotateWithQTProlongating(medicationName: String): QTProlongatingRisk {
        val riskConfigs = this.qtProlongatingCuration.curate(medicationName)
        return if (riskConfigs.isEmpty()) {
            QTProlongatingRisk.NONE
        } else if (riskConfigs.size > 1) {
            throw IllegalStateException(
                "Multiple risk configurations found for one medication name [$medicationName]. " +
                        "Check the qt_prolongating.tsv for a duplicate"
            )
        } else {
            return riskConfigs.first().status
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
            ExtractionResult(null, ExtractionEvaluation())
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
            curationService: CurationService,
            atcModel: AtcModel
        ) =
            MedicationExtractor(
                medicationNameCuration = curationService.medicationNameCuration,
                medicationDosageCuration = curationService.medicationDosageCuration,
                periodBetweenUnitCuration = curationService.periodBetweenUnitCuration,
                cypInterationCuration = curationService.cypInteractionCuration,
                qtProlongatingCuration = curationService.qtProlongingCuration,
                administrationRouteTranslation = curationService.administrationRouteTranslation,
                dosageUnitTranslation = curationService.dosageUnitTranslation,
                atcModel = atcModel
            )
    }
}