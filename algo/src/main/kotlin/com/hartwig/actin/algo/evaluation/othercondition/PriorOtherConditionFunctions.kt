package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.icd.datamodel.IcdMatches
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.IcdModel

object PriorOtherConditionFunctions {

    fun findRelevantPriorConditionsMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<PriorOtherCondition> {

        val relevant = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
        val matches = IcdModel.findInstancesMatchingAnyIcdCode(icdModel, relevant, targetIcdCodes)

        return IcdMatches(
            fullMatches = matches.fullMatches.filterIsInstance<PriorOtherCondition>(),
            mainCodeMatchesWithUnknownExtension = matches.mainCodeMatchesWithUnknownExtension.filterIsInstance<PriorOtherCondition>()
        )
    }
}