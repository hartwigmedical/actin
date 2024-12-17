package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceWithSpecificIcdTitle(private val icdModel: IcdModel, private val targetIcdTitle: String) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCode = icdModel.resolveCodeForTitle(targetIcdTitle)!!
        val matchingAllergies = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(icdModel, record, setOf(targetCode))
            .fullMatches.map { it.name }

        return if (matchingAllergies.isNotEmpty()) {
            EvaluationFactory.pass("Has allergy ${concat(matchingAllergies)} belonging to $targetIcdTitle")
        } else {
            EvaluationFactory.fail(
                "No allergies belonging to ${concat(matchingAllergies)}"
            )
        }
    }
}