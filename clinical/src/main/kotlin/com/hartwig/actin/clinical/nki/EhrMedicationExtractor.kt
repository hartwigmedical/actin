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
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
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
                qtPrologatingRiskCuration.find(it.drugName),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.QT_PROLONGATION,
                it.drugName,
                "medication name",
                true
            )
            val curatedCyp = CurationResponse.createFromConfigs(
                cypInteractionCuration.find(it.drugName),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.CYP_INTERACTION,
                it.drugName,
                "medication name",
                true
            )
            val curatedDosage = CurationResponse.createFromConfigs(
                dosageCuration.find(it.drugName),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.MEDICATION_DOSAGE,
                it.drugName,
                "medication name",
                true
            )
            val medication = ImmutableMedication.builder().name(it.drugName).administrationRoute(it.administrationRoute)
                .dosage(
                    curatedDosage.config()?.curated ?: ImmutableDosage.builder().dosageMax(it.dosage).dosageMin(it.dosage)
                        .dosageUnit(it.dosageUnit).frequency(it.frequency)
                        .frequencyUnit(it.frequencyUnit)
                        .periodBetweenValue(it.periodBetweenDosagesValue)
                        .periodBetweenUnit(it.periodBetweenDosagesUnit)
                        .ifNeeded(it.administrationOnlyIfNeeded)
                        .build()
                )
                .startDate(it.startDate)
                .stopDate(it.endDate)
                .atc(atcClassification)
                .qtProlongatingRisk(curatedQT.config()?.status ?: QTProlongatingRisk.UNKNOWN)
                .cypInteractions(curatedCyp.config()?.interactions ?: emptyList())
                .isTrialMedication(false)
                .isSelfCare(false)
                .build()
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