package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasSpecificInfection(private val doidModel: DoidModel, private val doidToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToFind)
        val hasSpecificInfection = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .flatMap { it.doids }
            .flatMap { doidModel.doidWithParents(it) }
            .contains(doidToFind)

        return if (hasSpecificInfection) {
            EvaluationFactory.pass("Has $doidTerm infection")
        } else {
            EvaluationFactory.fail("Has no known infection with $doidTerm")
        }
    }
}