package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalStatus

class EhrClinicalStatusExtractor : EhrExtractor<ClinicalStatus> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxBy { who -> who.evaluationDate }
        val clinicalStatus = ClinicalStatus(who = mostRecentWho.status, hasComplications = ehrPatientRecord.complications.isNotEmpty())
        return ExtractionResult(clinicalStatus, ExtractionEvaluation())
    }
}