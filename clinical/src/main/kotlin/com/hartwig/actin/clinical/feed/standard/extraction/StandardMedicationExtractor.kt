package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.medication.MedicationCategories
import org.apache.logging.log4j.LogManager

class StandardMedicationExtractor(
    private val atcModel: AtcModel,
    private val drugInteractionsDatabase: DrugInteractionsDatabase,
    private val qtProlongatingDatabase: QtProlongatingDatabase,
    private val treatmentDatabase: TreatmentDatabase
) : StandardDataExtractor<List<Medication>?> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Medication>?> {
        return ehrPatientRecord.medications?.map {
            val atcClassification = if (!it.isTrial && !it.isSelfCare) {
                if (it.atcCode == null) {
                    logger.error(
                        "Patient '${ehrPatientRecord.patientDetails.hashedId}' had medication '${it.name}' with null atc code, " +
                                "but is not a trial or self care"
                    )
                }
                it.atcCode?.let { atcCode -> atcModel.resolveByCode(atcCode, "") }
            } else null
            val atcNameOrInput = atcClassification?.chemicalSubstance?.name ?: it.name
            val atcCode = it.atcCode
            val isAntiCancerMedication = MedicationCategories.isAntiCancerMedication(atcCode)
            val drug = treatmentDatabase.findDrugByAtcName(atcNameOrInput)

            val atcWarning = if (isAntiCancerMedication && drug == null) {
                CurationWarning(
                    ehrPatientRecord.patientDetails.hashedId,
                    CurationCategory.MEDICATION_NAME,
                    atcNameOrInput,
                    "Anti cancer medication $atcNameOrInput with ATC code $atcCode found which is not present in drug database. Please add the missing drug to drug database"
                )
            } else null

            val medication = Medication(
                name = atcNameOrInput,
                administrationRoute = it.administrationRoute,
                dosage = Dosage(
                    dosageMin = it.dosage, dosageMax = it.dosage,
                    dosageUnit = it.dosageUnit, frequency = it.frequency, frequencyUnit = it.frequencyUnit,
                    periodBetweenValue = it.periodBetweenDosagesValue, periodBetweenUnit = it.periodBetweenDosagesUnit,
                    ifNeeded = it.administrationOnlyIfNeeded
                ),
                startDate = it.startDate,
                stopDate = it.endDate,
                atc = atcClassification,
                qtProlongatingRisk = qtProlongatingDatabase.annotateWithQTProlongating(atcNameOrInput),
                cypInteractions = drugInteractionsDatabase.annotateWithCypInteractions(atcNameOrInput),
                transporterInteractions = drugInteractionsDatabase.annotateWithTransporterInteractions(atcNameOrInput),
                isTrialMedication = it.isTrial,
                isSelfCare = it.isSelfCare,
                drug = drug
            )
            ExtractionResult(listOf(medication), CurationExtractionEvaluation(setOfNotNull(atcWarning)))
        }?.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult((acc.extracted ?: emptyList()) + result.extracted, acc.evaluation + result.evaluation)
        } ?: ExtractionResult(null, CurationExtractionEvaluation())
    }

    companion object {
        private val logger = LogManager.getLogger(StandardMedicationExtractor::class.java)
    }
}