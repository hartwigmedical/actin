package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.feed.datamodel.FeedPatientRecord
import org.apache.logging.log4j.LogManager

class StandardMedicationExtractor(
    private val atcModel: AtcModel,
    private val drugInteractionsDatabase: DrugInteractionsDatabase,
    private val qtProlongatingDatabase: QtProlongatingDatabase,
    private val treatmentDatabase: TreatmentDatabase
) : StandardDataExtractor<List<Medication>?> {

    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<Medication>?> {
        return ehrPatientRecord.medications?.map { feedMedication ->
            val isTrialMedication = feedMedication.isTrial || feedMedication.name.contains("(studie)", ignoreCase = true)
            val isUnspecifiedTrialMedication = feedMedication.name.contains("studiemedicatie", ignoreCase = true) && !isTrialMedication
            val atcClassification = if (!isTrialMedication && !isUnspecifiedTrialMedication && !feedMedication.isSelfCare) {
                if (feedMedication.atcCode == null) {
                    logger.error(
                        "Patient '${ehrPatientRecord.patientDetails.patientId}' had medication '${feedMedication.name}' with null atc code, " +
                                "but is not a trial or self care"
                    )
                }
                feedMedication.atcCode?.let { atcCode -> atcModel.resolveByCode(atcCode, "") }
            } else null
            val atcNameOrInput = atcClassification?.chemicalSubstance?.name ?: trimmedName(feedMedication.name)
            val atcCode = feedMedication.atcCode
            val isAntiCancerMedication = MedicationCategories.isAntiCancerMedication(atcCode)
            val drug = treatmentDatabase.findDrugByAtcName(atcNameOrInput)

            val atcWarning = if (isAntiCancerMedication && drug == null && !isUnspecifiedTrialMedication) {
                CurationWarning(
                    ehrPatientRecord.patientDetails.patientId,
                    CurationCategory.MEDICATION_NAME,
                    atcNameOrInput,
                    "Anti cancer medication or supportive trial medication $atcNameOrInput with ATC code $atcCode found which is not " +
                            "present in drug database. Please add the missing drug to drug database"
                )
            } else null

            val medication = Medication(
                name = atcNameOrInput,
                administrationRoute = feedMedication.administrationRoute,
                dosage = feedMedication.dosage.let { dosage ->
                    Dosage(
                        dosageMin = dosage?.dosageMin,
                        dosageMax = dosage?.dosageMax,
                        dosageUnit = dosage?.dosageUnit,
                        frequency = dosage?.frequency,
                        frequencyUnit = dosage?.frequencyUnit,
                        periodBetweenValue = dosage?.periodBetweenValue,
                        periodBetweenUnit = dosage?.periodBetweenUnit,
                        ifNeeded = dosage?.ifNeeded
                    )
                },
                startDate = feedMedication.startDate,
                stopDate = feedMedication.endDate,
                atc = atcClassification,
                qtProlongatingRisk = qtProlongatingDatabase.annotateWithQTProlongating(atcNameOrInput),
                cypInteractions = drugInteractionsDatabase.annotateWithCypInteractions(atcNameOrInput),
                transporterInteractions = drugInteractionsDatabase.annotateWithTransporterInteractions(atcNameOrInput),
                isTrialMedication = isTrialMedication,
                isSelfCare = feedMedication.isSelfCare,
                drug = drug
            )
            ExtractionResult(listOf(medication), CurationExtractionEvaluation(setOfNotNull(atcWarning)))
        }?.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult((acc.extracted ?: emptyList()) + result.extracted, acc.evaluation + result.evaluation)
        } ?: ExtractionResult(null, CurationExtractionEvaluation())
    }

    private fun trimmedName(name: String): String {
        return name.removeSuffix(" (STUDIE)").trim()
    }

    companion object {
        private val logger = LogManager.getLogger(StandardMedicationExtractor::class.java)
    }
}