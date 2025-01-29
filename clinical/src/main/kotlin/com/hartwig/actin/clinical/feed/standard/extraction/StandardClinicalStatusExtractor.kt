package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure

class StandardClinicalStatusExtractor(private val ecgCuration: CurationDatabase<ECGConfig>) : StandardDataExtractor<ClinicalStatus> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxByOrNull { who -> who.evaluationDate }
        val ecg = ehrPatientRecord.priorOtherConditions.map {
            CurationResponse.createFromConfigs(
                ecgCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.ECG,
                it.name,
                "ecg",
                true
            )
        }.firstNotNullOfOrNull { it.config() }
        val clinicalStatus = ClinicalStatus(
            who = mostRecentWho?.status,
            hasComplications = ehrPatientRecord.complications.isNotEmpty(),
            ecg = ecg?.let {
                Ecg(
                    jtcMeasure = maybeECGMeasure(it.jtcValue, it.jtcUnit),
                    qtcfMeasure = maybeECGMeasure(it.qtcfValue, it.qtcfUnit),
                    name = it.interpretation,
                    hasSigAberrationLatestECG = true
                )
            })
        return ExtractionResult(clinicalStatus, CurationExtractionEvaluation())
    }

    private fun maybeECGMeasure(value: Int?, unit: String?): EcgMeasure? {
        return if (value == null || unit == null) {
            null
        } else EcgMeasure(value = value, unit = unit)
    }
}