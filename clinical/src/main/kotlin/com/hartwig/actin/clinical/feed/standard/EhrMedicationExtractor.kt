package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CypInteractionCurationUtil
import com.hartwig.actin.clinical.curation.QTProlongatingCurationUtil
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication

class EhrMedicationExtractor(
    private val atcModel: AtcModel,
    private val qtProlongatingRiskCuration: CurationDatabase<QTProlongatingConfig>,
    private val cypInteractionCuration: CurationDatabase<CypInteractionConfig>,
    private val dosageCuration: CurationDatabase<MedicationDosageConfig>
) : EhrExtractor<List<Medication>> {

    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Medication>> {
        return ehrPatientRecord.medications.map {
            val atcClassification = atcModel.resolveByCode(it.atcCode, "")
            val atcNameOrInput = atcClassification?.chemicalSubstance?.name ?: it.name
            val curatedQT = QTProlongatingCurationUtil.annotateWithQTProlongating(qtProlongatingRiskCuration, atcNameOrInput)
            val curatedCyp = CypInteractionCurationUtil.curateMedicationCypInteractions(cypInteractionCuration, atcNameOrInput)
            val curatedDosage = CurationResponse.createFromConfigs(
                dosageCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedIdBase64(),
                CurationCategory.MEDICATION_DOSAGE,
                it.name,
                "dosage",
                true
            )
            val medication = Medication(
                name = atcNameOrInput,
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
                qtProlongatingRisk = curatedQT,
                cypInteractions = curatedCyp,
                isTrialMedication = false,
                isSelfCare = false
            )
            ExtractionResult(listOf(medication),
                listOf(
                    ExtractionResult(curatedDosage.config(), curatedDosage.extractionEvaluation)
                )
                    .fold(CurationExtractionEvaluation()) { acc, result -> acc + result.evaluation })
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }
}