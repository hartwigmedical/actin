package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.feed.datamodel.FeedMedication
import org.apache.logging.log4j.LogManager

class MedicationExtractor(
    private val medicationNameCuration: CurationDatabase<MedicationNameConfig>,
    private val medicationDosageCuration: CurationDatabase<MedicationDosageConfig>,
    private val periodBetweenUnitCuration: CurationDatabase<PeriodBetweenUnitConfig>,
    private val drugInteractionsDatabase: DrugInteractionsDatabase,
    private val qtProlongatingDatabase: QtProlongatingDatabase,
    private val administrationRouteTranslation: TranslationDatabase<String>,
    private val dosageUnitTranslation: TranslationDatabase<String>,
    private val atcModel: AtcModel,
    private val treatmentDatabase: TreatmentDatabase
) {

    fun extract(patientId: String, entries: List<FeedMedication>?): ExtractionResult<List<Medication>?> {

        return entries?.map { entry ->
            val nameCuration = curateName(entry, patientId)
            val name = nameCuration.extracted
            if (name.isNullOrEmpty()) ExtractionResult(emptyList(), nameCuration.evaluation) else {
                val administrationRouteCuration = translateAdministrationRoute(patientId, entry.administrationRoute)
                val dosage = curateDosage(administrationRouteCuration.extracted, entry, patientId)

                val atcCode = entry.atcCode
                val atc = atcModel.resolveByCode(entry.atcCode.orEmpty(), entry.atcCodeDisplay.orEmpty())
                val drug = treatmentDatabase.findDrugByAtcName(name)
                val isAntiCancerMedication = MedicationCategories.isAntiCancerMedication(atcCode)

                if (atc == null && !entry.isSelfCare && !entry.isTrial) {
                    LOGGER.error("Medication $name has no ATC code and is not self-care or a trial")
                }

                val atcWarning = if (isAntiCancerMedication && drug == null) {
                    ExtractionResult(
                        emptyList<Medication>(),
                        CurationExtractionEvaluation(
                            setOf(
                                CurationWarning(
                                    patientId,
                                    CurationCategory.MEDICATION_NAME,
                                    name,
                                    "Anti cancer medication $name with ATC code $atcCode found which is not present in drug database. " +
                                            "Please add the missing drug to drug database"
                                )
                            )
                        )
                    )
                } else null

                val medication = Medication(
                    dosage = dosage.extracted,
                    name = name,
                    status = curateMedicationStatus(patientId, entry.status.orEmpty()),
                    administrationRoute = administrationRouteCuration.extracted,
                    startDate = entry.startDate,
                    stopDate = entry.endDate,
                    cypInteractions = drugInteractionsDatabase.annotateWithCypInteractions(name),
                    transporterInteractions = drugInteractionsDatabase.annotateWithTransporterInteractions(name),
                    qtProlongatingRisk = qtProlongatingDatabase.annotateWithQTProlongating(name),
                    atc = atc,
                    isSelfCare = entry.isSelfCare,
                    isTrialMedication = entry.isTrial,
                    drug = drug
                )

                val evaluation = listOfNotNull(nameCuration, administrationRouteCuration, dosage, atcWarning)
                    .fold(CurationExtractionEvaluation()) { acc, result -> acc + result.evaluation }
                ExtractionResult(listOf(medication), evaluation)
            }
        }?.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted.orEmpty() + result.extracted, acc.evaluation + result.evaluation)
        } ?: ExtractionResult(null, CurationExtractionEvaluation())
    }

    private fun curateName(entry: FeedMedication, patientId: String): ExtractionResult<String?> {
        val atcName = CurationUtil.capitalizeFirstLetterOnly(entry.atcCodeDisplay)
        return if (atcName.isNotEmpty()) {
            ExtractionResult(atcName, CurationExtractionEvaluation())
        } else {
            val input = fullTrim(entry.name)
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
        administrationRoute: String?, entry: FeedMedication, patientId: String
    ): ExtractionResult<Dosage> {
        return if (dosageRequiresCuration(administrationRoute, entry)) {
            val input = entry.dosageInstruction.orEmpty().trim { it <= ' ' }
            val curationResponse = CurationResponse.createFromConfigs(
                medicationDosageCuration.find(input),
                patientId,
                CurationCategory.MEDICATION_DOSAGE,
                input,
                "medication dosage",
                true
            )
            ExtractionResult(curationResponse.config()?.curated ?: Dosage(), curationResponse.extractionEvaluation)
        } else {
            val dosageUnitTranslation = translateDosageUnit(patientId, entry.dosage?.dosageUnit)
            val periodBetweenUnitCuration = curatePeriodBetweenUnit(patientId, entry.dosage?.periodBetweenUnit)
            ExtractionResult(
                Dosage(
                    dosageMin = entry.dosage?.dosageMin,
                    dosageMax = entry.dosage?.dosageMax,
                    dosageUnit = dosageUnitTranslation.extracted,
                    frequency = entry.dosage?.frequency,
                    frequencyUnit = entry.dosage?.frequencyUnit,
                    periodBetweenValue = entry.dosage?.periodBetweenValue,
                    periodBetweenUnit = entry.dosage?.periodBetweenUnit,
                    ifNeeded = entry.dosage?.ifNeeded,
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

    private fun dosageRequiresCuration(administrationRoute: String?, entry: FeedMedication) =
        entry.dosage?.let { dosage ->
            administrationRoute?.lowercase() == "oral" && (dosage.dosageMin == 0.0 ||
                    dosage.dosageUnit.isNullOrEmpty() ||
                    dosage.frequency == 0.0 ||
                    dosage.frequencyUnit.isNullOrEmpty())
        } ?: throw IllegalStateException("Dosage information missing for medication '${entry.name}'")


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
            atcModel: AtcModel,
            drugInteractionsDatabase: DrugInteractionsDatabase,
            qtProlongatingDatabase: QtProlongatingDatabase,
            treatmentDatabase: TreatmentDatabase
        ) =
            MedicationExtractor(
                medicationNameCuration = curationDatabaseContext.medicationNameCuration,
                medicationDosageCuration = curationDatabaseContext.medicationDosageCuration,
                periodBetweenUnitCuration = curationDatabaseContext.periodBetweenUnitCuration,
                drugInteractionsDatabase = drugInteractionsDatabase,
                qtProlongatingDatabase = qtProlongatingDatabase,
                administrationRouteTranslation = curationDatabaseContext.administrationRouteTranslation,
                dosageUnitTranslation = curationDatabaseContext.dosageUnitTranslation,
                atcModel = atcModel,
                treatmentDatabase = treatmentDatabase
            )
    }
}