package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.doid.DoidModel

class HasSpecificInfection(private val doidModel: DoidModel, private val doidToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToFind)
        val hasSpecificInfection = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .flatMap { it.doids }
            .flatMap { doidModel.doidWithParents(it) }
            .contains(doidToFind)

        return if (hasSpecificInfection) {
            EvaluationFactory.pass("Patient has infection with $doidTerm", "Present $doidTerm infection")
        } else {
            EvaluationFactory.fail("Patient has no known infection with $doidTerm", "Specific infection(s) not present")
        }
    }
}