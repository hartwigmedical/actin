package com.hartwig.actin.algo.othercondition

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.IcdModel

object OtherConditionSelector {

    fun selectClinicallyRelevant(conditions: List<PriorOtherCondition>): List<PriorOtherCondition> {
        return conditions.filter { it.isContraindicationForTherapy }
    }

    fun selectConditionsMatchingIcdCode(conditions: List<PriorOtherCondition>, icdCodes: List<String>, icdModel: IcdModel): Set<String> {
        return selectClinicallyRelevant(conditions).filter { icdModel.returnCodeWithParents(it.icdCode).any { code -> code in icdCodes } }
            .map(PriorOtherCondition::name)
            .toSet()
    }
}