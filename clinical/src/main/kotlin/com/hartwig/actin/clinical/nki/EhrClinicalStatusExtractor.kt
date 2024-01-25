package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus

class EhrClinicalStatusExtractor : EhrExtractor<ClinicalStatus> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxBy { who -> who.evaluationDate }
        val clinicalStatus =
            ImmutableClinicalStatus.builder().who(mostRecentWho.status).hasComplications(ehrPatientRecord.complications.isNotEmpty())
                .build()
        return ExtractionResult(clinicalStatus, ExtractionEvaluation())
    }
}