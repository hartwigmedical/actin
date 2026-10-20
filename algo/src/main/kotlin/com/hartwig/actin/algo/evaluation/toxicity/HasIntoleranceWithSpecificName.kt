package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasIntoleranceWithSpecificName(private val termToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTerm = termToFind.lowercase()
        val allergies = record.intolerances.filter { it.name?.lowercase()?.contains(lowercaseTerm) == true }.toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.pass("Allergy " + Format.concatItemsWithAnd(allergies))
        } else {
            EvaluationFactory.fail("No allergies with name $termToFind")
        }
    }
}