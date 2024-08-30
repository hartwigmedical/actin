package com.hartwig.actin.algo.othercondition

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.doid.DoidModel

object OtherConditionSelector {
    fun selectClinicallyRelevant(conditions: List<PriorOtherCondition>): List<PriorOtherCondition> {
        return conditions.filter { it.isContraindicationForTherapy }
    }

    fun selectConditionsMatchingDoid(conditions: List<PriorOtherCondition>, doidToFind: String, doidModel: DoidModel): Set<String> {
        return selectClinicallyRelevant(conditions).filter { conditionHasDoid(it, doidToFind, doidModel) }
            .map(PriorOtherCondition::name)
            .toSet()
    }

    private fun conditionHasDoid(condition: PriorOtherCondition, doidToFind: String, doidModel: DoidModel): Boolean {
        return condition.doids.any { doidModel.doidWithParents(it).contains(doidToFind) }
    }
}