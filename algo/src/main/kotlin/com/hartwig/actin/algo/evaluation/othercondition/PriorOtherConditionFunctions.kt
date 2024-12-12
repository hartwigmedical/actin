package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.IcdCodeMatcher
import com.hartwig.actin.algo.evaluation.IcdMatches
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.IcdModel

object PriorOtherConditionFunctions: IcdCodeMatcher {

    fun findPriorOtherConditionsMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<PriorOtherCondition> {

        return findInstancesMatchingAnyIcdCode(icdModel, record.priorOtherConditions, targetIcdCodes)
    }
}