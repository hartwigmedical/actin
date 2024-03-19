package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.doid.DoidModel

class HasIntoleranceWithSpecificDoid(private val doidModel: DoidModel, private val doidToFind: String) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.intolerances
            .filter { intolerance -> intolerance.doids.flatMap { doidModel.doidWithParents(it) }.contains(doidToFind) }
            .map { it.name }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has allergy " + concat(allergies) + " belonging to " + doidModel.resolveTermForDoid(doidToFind),
                "Present allergy " + concat(allergies) + " belonging to " + doidModel.resolveTermForDoid(doidToFind)
            )
        } else {
            EvaluationFactory.fail(
                "Patient has no allergies with doid" + doidModel.resolveTermForDoid(doidToFind),
                "No allergies belonging to " + doidModel.resolveTermForDoid(doidToFind)
            )
        }
    }
}