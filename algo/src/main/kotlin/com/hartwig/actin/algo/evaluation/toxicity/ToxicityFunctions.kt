package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

object ToxicityFunctions {

    fun selectRelevantToxicities(
        record: PatientRecord, icdModel: IcdModel, referenceDate: LocalDate, icdTitlesToIgnore: List<String> = emptyList()
    ): List<Toxicity> {
        val complicationIcdCodes = record.complications?.map(Complication::icdCode)?.toSet() ?: emptySet()
        val ignoredIcdMainCodes = icdTitlesToIgnore.mapNotNull(icdModel::resolveCodeForTitle).map { it.mainCode }.toSet()

        return dropOutdatedEHRToxicities(record.toxicities)
            .filter { it.endDate?.isAfter(referenceDate) != false }
            .filter { it.source != ToxicitySource.EHR || it.icdCode !in complicationIcdCodes }
            .filterNot { ignoredIcdMainCodes.contains(it.icdCode.mainCode) }
    }

    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
        val mostRecentEhrToxicitiesByCode = ehrToxicities.groupBy(Toxicity::icdCode)
            .map { (_, toxGroup) -> toxGroup.maxBy(Toxicity::evaluatedDate) }
        return otherToxicities + mostRecentEhrToxicitiesByCode
    }
}