package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasIntoleranceWithSpecificDoid(private val doidModel: DoidModel, private val doidToFind: String) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.intolerances
            .filter { intolerance -> intolerance.doids.flatMap { doidModel.doidWithParents(it) }.contains(doidToFind) }
            .map { it.name }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.pass(
                "Present allergy " + Format.concatLowercaseWithCommaAndAnd(allergies) + " belonging to " +
                        doidModel.resolveTermForDoid(doidToFind)
            )
        } else {
            EvaluationFactory.fail("No allergies belonging to " + doidModel.resolveTermForDoid(doidToFind))
        }
    }
}