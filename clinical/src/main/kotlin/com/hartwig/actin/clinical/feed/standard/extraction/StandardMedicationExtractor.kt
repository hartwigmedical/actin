package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.DrugInteractionCurationUtil
import com.hartwig.actin.clinical.curation.QTProlongatingCurationUtil
import com.hartwig.actin.clinical.curation.config.DrugInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication

class StandardMedicationExtractor(
    private val atcModel: AtcModel,
    private val treatmentDatabase: TreatmentDatabase,
    private val qtProlongatingRiskCuration: CurationDatabase<QTProlongatingConfig>,
    private val drugInteractionCuration: CurationDatabase<DrugInteractionConfig>
) : StandardDataExtractor<List<Medication>?> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Medication>?> {
        return ExtractionResult(ehrPatientRecord.medications?.map {
            val atcClassification = if (!it.isTrial && !it.isSelfCare) atcModel.resolveByCode(
                it.atcCode
                    ?: throw IllegalStateException(
                        "Patient '${ehrPatientRecord.patientDetails.hashedId}' had medication '${it.name}' with null atc code, " +
                                "but is not a trial or self care"
                    ),
                ""
            ) else null
            val atcNameOrInput = atcClassification?.chemicalSubstance?.name ?: it.name
            Medication(
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
                qtProlongatingRisk = QTProlongatingCurationUtil.annotateWithQTProlongating(qtProlongatingRiskCuration, atcNameOrInput),
                cypInteractions = DrugInteractionCurationUtil.curateMedicationCypInteractions(drugInteractionCuration, atcNameOrInput),
                transporterInteractions = DrugInteractionCurationUtil.curateMedicationTransporterInteractions(
                    drugInteractionCuration,
                    atcNameOrInput
                ),
                isTrialMedication = it.isTrial,
                isSelfCare = it.isSelfCare,
                drug = treatmentDatabase.findDrugByAtcName(atcNameOrInput)
            )
        }, CurationExtractionEvaluation())
    }
}