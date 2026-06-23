package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import java.time.LocalDate

object ToxicityFunctions {

    fun selectRelevantToxicities(
        record: PatientRecord, referenceDate: LocalDate, ignoredIcdMainCodes: Set<String> = emptySet()
    ): List<Toxicity> {
        val icdCodesToExclude = record.otherConditions.map(Comorbidity::icdCodes).toSet()

        return dropOutdatedEHRToxicities(record.toxicities)
            .filter { DateComparison.isBeforeDate(referenceDate.minusYears(2), it.year, it.month) != true }
            .filter { it.source != ToxicitySource.EHR || it.icdCodes !in icdCodesToExclude }
            .filterNot { it.icdCodes.any { code -> code.mainCode in ignoredIcdMainCodes } }
    }

    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
        val mostRecentEhrToxicitiesByCode = ehrToxicities
            .groupBy(Toxicity::icdCodes)
            .map { (_, toxGroup) -> toxGroup.maxWith(compareBy<Toxicity> { it.year }.thenBy { it.month }.thenBy { it.day }) }

        return otherToxicities + mostRecentEhrToxicitiesByCode
    }
}