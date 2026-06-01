package com.hartwig.actin.algo.evaluation.toxicity

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
            .filter { it.evaluatedDate?.isBefore(referenceDate.minusYears(2)) != true }
            .filter { it.source != ToxicitySource.EHR || it.icdCodes !in icdCodesToExclude }
            .filterNot { it.icdCodes.any { code -> code.mainCode in ignoredIcdMainCodes } }
    }

    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
        val mostRecentEhrToxicitiesByCode = ehrToxicities
            .groupBy(Toxicity::icdCodes)
            .map { (_, toxGroup) ->
                toxGroup.maxBy { it.evaluatedDate ?: LocalDate.MIN }
            }

        return otherToxicities + mostRecentEhrToxicitiesByCode
    }
}