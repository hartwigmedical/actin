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
        val ignoredIcdCodes = icdTitlesToIgnore.mapNotNull(icdModel::resolveCodeForTitle).toSet()

        return dropOutdatedEHRToxicities(record.toxicities)
            .filter { it.endDate?.isAfter(referenceDate) != false }
            .filter { it.source != ToxicitySource.EHR || it.icdCode !in complicationIcdCodes }
            .filterNot { it.icdCode in ignoredIcdCodes }
    }

    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
        val mostRecentEhrToxicitiesByCode = ehrToxicities.groupBy(Toxicity::icdCode)
            .map { (_, toxGroup) -> toxGroup.maxBy(Toxicity::evaluatedDate) }
        return otherToxicities + mostRecentEhrToxicitiesByCode
    }

    fun hasIcdMatch(toxicity: Toxicity, targetIcdTitles: List<String>?, icdModel: IcdModel): Boolean {
        if (targetIcdTitles == null) return true
        val targetIcdCodes = targetIcdTitles.mapNotNull { icdModel.resolveCodeForTitle(it) }
        return targetIcdCodes.any { it in icdModel.returnCodeWithParents(toxicity.icdCode) }
    }
}