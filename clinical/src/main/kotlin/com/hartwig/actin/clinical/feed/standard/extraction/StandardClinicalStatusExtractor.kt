package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.ClinicalStatus

class StandardClinicalStatusExtractor : StandardDataExtractor<ClinicalStatus> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxByOrNull { who -> who.evaluationDate }
        val clinicalStatus = ClinicalStatus(who = mostRecentWho?.status, hasComplications = ehrPatientRecord.complications.isNotEmpty())
        return ExtractionResult(clinicalStatus, CurationExtractionEvaluation())
    }
}