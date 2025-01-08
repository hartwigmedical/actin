package com.hartwig.actin.algo.othercondition

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition

object OtherConditionSelector {

    fun selectClinicallyRelevant(conditions: List<PriorOtherCondition>): List<PriorOtherCondition> {
        return conditions.filter { it.isContraindicationForTherapy }
    }
}