package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.IcdModel

object PriorOtherConditionFunctions {

    fun findPriorOtherConditionsMatchingAnyIcdCode(record: PatientRecord, icdCodes: List<String>, icdModel: IcdModel): List<PriorOtherCondition> {
        return record.priorOtherConditions.filter { icdModel.returnCodeWithParents(it.icdCode).any { code -> code in icdCodes } }
    }
}