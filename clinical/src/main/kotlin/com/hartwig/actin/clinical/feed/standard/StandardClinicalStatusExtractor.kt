package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.ClinicalStatus

class StandardClinicalStatusExtractor : StandardDataExtractor<ClinicalStatus> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxByOrNull { who -> who.evaluationDate }
        val whoAsInteger = mostRecentWho?.let { parseRangeAndUseLowerValue(it.status) }
        val clinicalStatus = ClinicalStatus(who = whoAsInteger, hasComplications = ehrPatientRecord.complications.isNotEmpty())
        return ExtractionResult(clinicalStatus, CurationExtractionEvaluation())
    }

    private fun parseRangeAndUseLowerValue(status: String): Int {
        val matchResult = "(\\d+)-(\\d+)".toRegex().matchEntire(status)
        return if (matchResult != null) {
            val (start, end) = matchResult.destructured
            minOf(start.toInt(), end.toInt())
        } else {
            status.toInt()
        }
    }
}