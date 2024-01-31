package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class EhrMedicationExtractor(
    private val atcModel: AtcModel,
    private val qtPrologatingRiskCuration: CurationDatabase<QTProlongatingConfig>,
    private val cypInteractionCuration: CurationDatabase<CypInteractionConfig>,
    private val dosageCuration: CurationDatabase<MedicationDosageConfig>
) : EhrExtractor<List<Medication>> {

    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Medication>> {
        return ehrPatientRecord.medications.map {
            val atcClassification = atcModel.resolveByCode(it.atcCode)
            val curatedQT = CurationResponse.createFromConfigs(
                qtPrologatingRiskCuration.find(it.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.QT_PROLONGATION,
                it.name,
                "qt prolongating risk",
                true
            )
            val curatedCyp = CurationResponse.createFromConfigs(
                cypInteractionCuration.find(it.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.CYP_INTERACTION,
                it.name,
                "cyp interaction",
                true
            )
            val curatedDosage = CurationResponse.createFromConfigs(
                dosageCuration.find(it.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.MEDICATION_DOSAGE,
                it.name,
                "dosage",
                true
            )
            val medication = Medication(
                name = it.name,
                administrationRoute = it.administrationRoute,
                dosage = curatedDosage.config()?.curated ?: Dosage(
                    dosageMin = it.dosage, dosageMax = it.dosage,
                    dosageUnit = it.dosageUnit, frequency = it.frequency, frequencyUnit = it.frequencyUnit,
                    periodBetweenValue = it.periodBetweenDosagesValue, periodBetweenUnit = it.periodBetweenDosagesUnit,
                    ifNeeded = it.administrationOnlyIfNeeded
                ),
                startDate = it.startDate,
                stopDate = it.endDate,
                atc = atcClassification,
                qtProlongatingRisk = curatedQT.config()?.status ?: QTProlongatingRisk.UNKNOWN,
                cypInteractions = curatedCyp.config()?.interactions ?: emptyList(),
                isTrialMedication = false,
                isSelfCare = false
            )
            ExtractionResult(listOf(medication),
                listOf(
                    ExtractionResult(curatedQT.config(), curatedQT.extractionEvaluation),
                    ExtractionResult(curatedCyp.config(), curatedCyp.extractionEvaluation)
                )
                    .fold(ExtractionEvaluation()) { acc, result -> acc + result.evaluation })
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }
}