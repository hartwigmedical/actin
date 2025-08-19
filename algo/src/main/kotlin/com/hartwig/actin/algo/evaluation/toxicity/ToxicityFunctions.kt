package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

object ToxicityFunctions {

    fun selectRelevantToxicities(
        record: PatientRecord, icdModel: IcdModel, referenceDate: LocalDate, icdTitlesToIgnore: List<String> = emptyList()
    ): List<Toxicity> {
        val icdCodesToExclude = (record.otherConditions + record.complications).map(Comorbidity::icdCodes).toSet()
        val ignoredIcdMainCodes = icdTitlesToIgnore.mapNotNull(icdModel::resolveCodeForTitle).map { it.mainCode }.toSet()

        return dropOutdatedEHRToxicities(record.toxicities)
            .filter { it.endDate?.isAfter(referenceDate) != false }
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