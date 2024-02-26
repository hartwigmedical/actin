package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalStatus

class EhrClinicalStatusExtractor : EhrExtractor<ClinicalStatus> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxByOrNull { who -> who.evaluationDate }
        val whoAsInteger = mostRecentWho?.let { parseRangeAndUserLower(it.status) }
        val clinicalStatus = ClinicalStatus(who = whoAsInteger, hasComplications = ehrPatientRecord.complications.isNotEmpty())
        return ExtractionResult(clinicalStatus, CurationExtractionEvaluation())
    }

    private fun parseRangeAndUserLower(status: String): Int {
        val matchResult = "(\\d+)-(\\d+)".toRegex().matchEntire(status)
        return if (matchResult != null) {
            val (start, end) = matchResult.destructured
            minOf(start.toInt(), end.toInt())
        } else {
            status.toInt()
        }
    }
}